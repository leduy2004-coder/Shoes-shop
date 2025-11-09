package com.java.shoes_service.service;

import com.java.CloudinaryResponse;
import com.java.ImageType;
import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.cart.CartGetResponse;
import com.java.shoes_service.dto.cart.CartItemResponse;
import com.java.shoes_service.dto.cart.CartCreateRequest;
import com.java.shoes_service.dto.cart.ProductCartResponse;
import com.java.shoes_service.entity.cart.CartEntity;
import com.java.shoes_service.entity.cart.CartItemEntity;
import com.java.shoes_service.entity.cart.Variant;
import com.java.shoes_service.entity.product.ProductEntity;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.CartItemRepository;
import com.java.shoes_service.repository.CartRepository;
import com.java.shoes_service.repository.httpClient.FileClient;
import com.java.shoes_service.repository.product.ProductRepository;
import com.java.shoes_service.repository.product.VariantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {

    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    VariantRepository variantRepository;
    ProductRepository productRepository;
    ModelMapper modelMapper;
    FileClient fileClient;
    // ---------- Public API ----------

    public CartGetResponse getCart(String userId) {
        CartEntity cart = getOrCreateCart(userId);
        sanitizeCartItemsAgainstStock(cart);      // dọn giỏ theo stock hiện tại
        return buildCartGetResponse(cart);
    }

    @Transactional
    public CartGetResponse addToCart(String userId, CartCreateRequest req) {
        if (req == null || req.getVariantId() == null || req.getVariantId().isBlank())
            throw new AppException(ErrorCode.INVALID_REQUEST);
        if (req.getQuantity() <= 0)
            throw new AppException(ErrorCode.INVALID_REQUEST);

        CartEntity cart = getOrCreateCart(userId);

        // Lấy variant + product
        var variant = variantRepository.findById(req.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        var product = productRepository.findById(variant.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        // Tìm dòng cùng variant
        Optional<CartItemEntity> existing = cartItemRepository.findByCartId(cart.getId()).stream()
                .filter(it -> it.getVariant() != null
                        && req.getVariantId().equals(it.getVariant().getId()))
                .findFirst();

        if (existing.isPresent()) {
            int newQty = existing.get().getQuantity() + req.getQuantity();
            if (newQty > variant.getStock())
                throw new AppException(ErrorCode.EXCEED_STOCK);

            existing.get().setQuantity(newQty);
            cartItemRepository.save(existing.get());
        } else {
            if (req.getQuantity() > variant.getStock())
                throw new AppException(ErrorCode.EXCEED_STOCK);

            // Nhúng Variant (embedded) vào CartItem
            Variant embedded = new Variant();
            embedded.setId(variant.getId());
            embedded.setProductId(variant.getProductId());
            embedded.setColor(variant.getColor());
            embedded.setSizeLabel(variant.getSize().getLabel());
            embedded.setStock(variant.getStock());

            CartItemEntity line = CartItemEntity.builder()
                    .cartId(cart.getId())
                    .productId(product.getId())
                    .variant(embedded)
                    .quantity(req.getQuantity())
                    .build();
            cartItemRepository.save(line);
        }

        updateCartSummary(cart);
        return buildCartGetResponse(cart);
    }

    @Transactional
    public CartGetResponse updateItemQuantity(String userId, String itemId, int quantity) {
        if (quantity <= 0) throw new AppException(ErrorCode.INVALID_REQUEST);

        CartItemEntity item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        CartEntity cart = cartRepository.findById(item.getCartId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        if (!cart.getUserId().equals(userId))
            throw new AppException(ErrorCode.FORBIDDEN);

        // Kiểm tra stock hiện tại
        if (item.getVariant() != null && item.getVariant().getId() != null) {
            var variant = variantRepository.findById(item.getVariant().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            if (quantity > variant.getStock())
                throw new AppException(ErrorCode.EXCEED_STOCK);
        } else {
            var product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
            if (quantity > product.getTotalStock())
                throw new AppException(ErrorCode.EXCEED_STOCK);
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        updateCartSummary(cart);
        return buildCartGetResponse(cart);
    }

    @Transactional
    public CartGetResponse deleteCartItem(String userId, String itemId) {
        CartItemEntity item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        CartEntity cart = cartRepository.findById(item.getCartId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        if (!cart.getUserId().equals(userId))
            throw new AppException(ErrorCode.FORBIDDEN);

        cartItemRepository.deleteById(itemId);
        updateCartSummary(cart);

        return buildCartGetResponse(cart);
    }

    @Transactional
    public void clearCart(String userId) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        cartItemRepository.deleteByCartId(cart.getId());
        cart.setCount(0);
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);
    }

    // ---------- Helpers ----------

    private CartEntity getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(
                        CartEntity.builder()
                                .userId(userId)
                                .count(0)
                                .totalPrice(0.0)
                                .build()
                ));
    }

    private CartGetResponse buildCartGetResponse(CartEntity cart) {
        // lấy toàn bộ items của cart
        List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            return CartGetResponse.builder()
                    .id(cart.getId())
                    .userId(cart.getUserId())
                    .count(cart.getCount())
                    .totalPrice(cart.getTotalPrice())
                    .items(List.of())
                    .build();
        }

        // batch product để tránh N+1
        List<String> productIds = items.stream()
                .map(CartItemEntity::getProductId)
                .distinct()
                .toList();

        Map<String, ProductEntity> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        // (tùy chọn) cache ảnh theo productId để tránh gọi lặp
        Map<String, CloudinaryResponse> firstImageCache = new java.util.HashMap<>();
        for (String pid : productIds) {
            try {
                var apiRes = fileClient.getImage(pid, ImageType.PRODUCT);
                var imgs = (apiRes != null) ? apiRes.getResult() : null;
                if (imgs != null && !imgs.isEmpty() && imgs.get(0) != null) {
                    firstImageCache.put(pid, imgs.get(0)); // giữ nguyên CloudinaryResponse
                }
            } catch (Exception ignore) {
                // có thể log cảnh báo nếu cần
            }
        }

        // build DTO trả về
        List<CartItemResponse> itemDtos = items.stream().map(it -> {
            ProductEntity p = productMap.get(it.getProductId());
            ProductCartResponse pDto = null;
            if (p != null) {
                pDto = modelMapper.map(p, ProductCartResponse.class);
                pDto.setImageUrl(firstImageCache.get(p.getId())); // có thể null nếu không có ảnh
            }

            CartItemResponse dto = new CartItemResponse();
            dto.setId(it.getId());
            dto.setCartId(it.getCartId());
            dto.setQuantity(it.getQuantity());
            dto.setVariant(it.getVariant());   // giữ embedded Variant trong cart item
            dto.setProduct(pDto);
            return dto;
        }).toList();

        return CartGetResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .count(cart.getCount())
                .totalPrice(cart.getTotalPrice())
                .items(itemDtos)
                .build();
    }


    /** Dọn giỏ: xoá item vượt stock / không hợp lệ rồi cập nhật count & totalPrice. */
    private void sanitizeCartItemsAgainstStock(CartEntity cart) {
        List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());
        if (items.isEmpty()) {
            cart.setCount(0);
            cart.setTotalPrice(0.0);
            cartRepository.save(cart);
            return;
        }

        // batch products
        List<String> productIds = items.stream().map(CartItemEntity::getProductId).distinct().toList();
        Map<String, ProductEntity> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        // batch variants
        List<String> variantIds = items.stream()
                .map(CartItemEntity::getVariant).filter(java.util.Objects::nonNull)
                .map(Variant::getId).filter(java.util.Objects::nonNull).distinct().toList();
        var variantMap = variantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(v -> v.getId(), v -> v));

        // xác định item cần xoá
        List<String> toDelete = items.stream().filter(it -> {
            ProductEntity p = productMap.get(it.getProductId());
            if (p == null || it.getQuantity() <= 0) return true;

            if (it.getVariant() != null && it.getVariant().getId() != null) {
                var v = variantMap.get(it.getVariant().getId());
                return (v == null) || !p.getId().equals(v.getProductId()) || it.getQuantity() > v.getStock();
            } else {
                return it.getQuantity() > p.getTotalStock();
            }
        }).map(CartItemEntity::getId).toList();

        if (!toDelete.isEmpty()) cartItemRepository.deleteAllById(toDelete);

        updateCartSummary(cart);
    }

    /** Cập nhật count & totalPrice. */
    private void updateCartSummary(CartEntity cart) {
        List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());
        cart.setCount(items.stream().mapToInt(CartItemEntity::getQuantity).sum());

        // batch product
        List<String> productIds = items.stream().map(CartItemEntity::getProductId).distinct().toList();
        Map<String, ProductEntity> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (CartItemEntity it : items) {
            ProductEntity p = productMap.get(it.getProductId());
            if (p == null) continue; // có thể vừa bị xoá/sai lệch
            total = total.add(effectiveUnitPrice(p)
                    .multiply(java.math.BigDecimal.valueOf(it.getQuantity())));
        }
        cart.setTotalPrice(total.doubleValue());
        cartRepository.save(cart);
    }

    /** Giá sau giảm (discount%). */
    private java.math.BigDecimal effectiveUnitPrice(ProductEntity p) {
        java.math.BigDecimal price = java.math.BigDecimal.valueOf(p.getPrice());
        double d = Math.min(100.0, Math.max(0.0, p.getDiscount()));
        if (d <= 0) return price;
        return price.multiply(java.math.BigDecimal.valueOf(1.0 - d / 100.0));
    }
}

