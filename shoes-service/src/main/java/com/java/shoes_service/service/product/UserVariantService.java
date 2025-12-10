package com.java.shoes_service.service.product;


import com.java.shoes_service.dto.product.product.ProductGetResponse;
import com.java.shoes_service.dto.product.product.UserPurchasedItemResponse;
import com.java.shoes_service.dto.product.variant.BuyVariantRequest;
import com.java.shoes_service.dto.product.variant.UserVariantRequest;
import com.java.shoes_service.dto.product.variant.UserVariantResponse;
import com.java.shoes_service.dto.product.variant.VariantResponse;
import com.java.shoes_service.entity.order.PurchaseOrderEntity;
import com.java.shoes_service.entity.product.ProductEntity;
import com.java.shoes_service.entity.product.UserVariantEntity;
import com.java.shoes_service.entity.product.VariantEntity;
import com.java.shoes_service.entity.product.VariantSizeEntity;
import com.java.shoes_service.entity.promotion.CouponEntity;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.entity.product.HistoryProductEntity;
import com.java.shoes_service.repository.order.PurchaseOrderRepository;
import com.java.shoes_service.repository.product.HistoryProductRepository;
import com.java.shoes_service.repository.product.ProductRepository;
import com.java.shoes_service.repository.product.UserVariantRepository;
import com.java.shoes_service.repository.product.VariantRepository;
import com.java.shoes_service.repository.product.VariantSizeRepository;
import com.java.shoes_service.repository.promotion.CouponRepository;
import com.java.shoes_service.utility.GetInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserVariantService {

    VariantRepository variantRepository;
    VariantSizeRepository variantSizeRepository;
    ProductRepository productRepository;
    UserVariantRepository userVariantRepository;
    PurchaseOrderRepository purchaseOrderRepository;
    CouponRepository couponRepository;
    HistoryProductRepository historyProductRepository;
    ModelMapper modelMapper;

    @Transactional
    public List<UserVariantResponse> buyVariantWithCoupon(BuyVariantRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        String userId = GetInfo.getLoggedInUserName();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<UserVariantRequest> items = request.getItems();
        String couponCode = request.getCouponCode();
        
        // 1. Validate và lấy coupon nếu có
        CouponEntity coupon = null;
        if (couponCode != null && !couponCode.isBlank()) {
            coupon = couponRepository.findByCode(couponCode.trim())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
            
            // Validate coupon
            if (!coupon.isActive()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            
            if (coupon.getExpirationDate() != null && coupon.getExpirationDate().isBefore(Instant.now())) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            
            if (coupon.getQuantity() <= 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }

        // 2. Tính tổng giá trước giảm và validate stock
        // item.getVariantId() giờ là variantSizeId (ID của VariantSizeEntity)
        double totalPrice = 0.0;
        List<String> variantSizeIds = new ArrayList<>();
        
        for (UserVariantRequest item : items) {
            VariantSizeEntity variantSize = variantSizeRepository.findById(item.getVariantSizeId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            
            VariantEntity variant = variantRepository.findById(variantSize.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            
            ProductEntity product = productRepository.findById(variant.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
            
            long quantity = item.getQuantity();
            if (quantity <= 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            
            if (variantSize.getStock() < quantity) {
                throw new AppException(ErrorCode.EXCEED_STOCK);
            }
            
            // Tính giá từ product (có thể đã có discount của product)
            double productPrice = product.getPrice();
            double productDiscount = product.getDiscount();
            double effectivePrice = productPrice * (1 - productDiscount / 100.0);
            
            totalPrice += effectivePrice * quantity;
            variantSizeIds.add(variantSize.getId());
        }

        // 3. Validate minOrder của coupon nếu có
        if (coupon != null && totalPrice < coupon.getMinOrder()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 4. Tính giá sau khi giảm (apply coupon)
        double finishPrice = totalPrice;
        Integer discountPercent = null;
        
        if (coupon != null) {
            discountPercent = coupon.getDiscountPercent();
            finishPrice = totalPrice * (1 - discountPercent / 100.0);
        }

        // 5. Xử lý từng variant và lưu UserVariantEntity
        List<UserVariantResponse> responses = new ArrayList<>();
        
        for (UserVariantRequest item : items) {
            VariantSizeEntity variantSize = variantSizeRepository.findById(item.getVariantSizeId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            
            VariantEntity variant = variantRepository.findById(variantSize.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            
            ProductEntity product = productRepository.findById(variant.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
            
            long quantity = item.getQuantity();
            
            // Tính giá cho variant này (phân bổ từ finishPrice)
            double itemPrice = 0.0;
            if (items.size() == 1) {
                itemPrice = finishPrice;
            } else {
                // Tính tỷ lệ giá của variant này so với tổng
                double productPrice = product.getPrice();
                double productDiscount = product.getDiscount();
                double effectivePrice = productPrice * (1 - productDiscount / 100.0);
                double itemTotalBeforeDiscount = effectivePrice * quantity;
                double ratio = itemTotalBeforeDiscount / totalPrice;
                itemPrice = finishPrice * ratio;
            }
            
            // Cập nhật VariantSizeEntity (stock và countSell)
            variantSize.setStock(variantSize.getStock() - (int) quantity);
            variantSize.setCountSell(variantSize.getCountSell() + (int) quantity);
            variantSizeRepository.save(variantSize);
            
            // Cập nhật Product (totalStock và countSell)
            product.setTotalStock(product.getTotalStock() - (int) quantity);
            product.setCountSell(product.getCountSell() + (int) quantity);
            productRepository.save(product);
            
            // Ghi history (giảm stock - count âm)
            HistoryProductEntity history = HistoryProductEntity.builder()
                    .variantSizeId(variantSize.getId())
                    .count(-(int) quantity) // Âm vì là xuất/giảm stock
                    .build();
            historyProductRepository.save(history);
            
            // Lưu UserVariantEntity (variantId giờ là variantSizeId)
            UserVariantEntity userVariant = UserVariantEntity.builder()
                    .userId(userId)
                    .variantSizeId(variantSize.getId()) // Lưu variantSizeId
                    .quantity(quantity)
                    .totalPrice(itemPrice)
                    .build();
            
            userVariant = userVariantRepository.save(userVariant);
            
            responses.add(UserVariantResponse.builder()
                    .id(userVariant.getId())
                    .variantId(userVariant.getVariantSizeId()) // variantId trong response là variantSizeId
                    .quantity(userVariant.getQuantity())
                    .totalMoney(userVariant.getTotalPrice())
                    .build());
        }

        // 6. Cập nhật quantity của coupon nếu có
        if (coupon != null) {
            coupon.setQuantity(coupon.getQuantity() - 1);
            couponRepository.save(coupon);
        }

        // 7. Lưu PurchaseOrderEntity (variantIds giờ là variantSizeIds)
        PurchaseOrderEntity purchaseOrder = PurchaseOrderEntity.builder()
                .userId(userId)
                .variantIds(variantSizeIds) // Lưu variantSizeIds
                .totalPrice(totalPrice)
                .finishPrice(finishPrice)
                .couponCode(couponCode)
                .discountPercent(discountPercent)
                .build();
        
        purchaseOrderRepository.save(purchaseOrder);

        return responses;
    }

    public List<UserPurchasedItemResponse> getPurchasedByUser(String userId) {
        List<UserVariantEntity> userVariants = userVariantRepository.findByUserId(userId);

        return userVariants.stream()
                .map(uv -> {
                    // uv.getVariantId() giờ là variantSizeId
                    VariantSizeEntity variantSize = variantSizeRepository.findById(uv.getVariantSizeId())
                            .orElse(null);
                    if (variantSize == null) {
                        log.warn("VariantSize not found for id={}", uv.getVariantSizeId());
                        return null;
                    }

                    VariantEntity variant = variantRepository.findById(variantSize.getVariantId())
                            .orElse(null);
                    if (variant == null) {
                        log.warn("Variant not found for id={}", variantSize.getVariantId());
                        return null;
                    }

                    ProductEntity product = productRepository.findById(variant.getProductId())
                            .orElse(null);
                    if (product == null) {
                        log.warn("Product not found for id={}", variant.getProductId());
                        return null;
                    }

                    ProductGetResponse productDto = modelMapper.map(product, ProductGetResponse.class);
                    
                    VariantResponse variantDto = VariantResponse.builder()
                            .id(variantSize.getId())
                            .productId(variant.getProductId())
                            .color(variant.getColor())
                            .status(variant.getStatus())
                            .size(variantSize.getSize())
                            .stock(variantSize.getStock())
                            .countSell(variantSize.getCountSell())
                            .build();

                    return UserPurchasedItemResponse.builder()
                            .product(productDto)
                            .variant(variantDto)
                            .countBuy(uv.getQuantity())
                            .totalMoney(uv.getTotalPrice())
                            .userId(uv.getUserId()) // Thêm userId để admin biết ai mua
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public List<UserPurchasedItemResponse> getPurchasedByUserFromToken() {
        String userId = GetInfo.getLoggedInUserName();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return getPurchasedByUser(userId);
    }

    public List<UserPurchasedItemResponse> getPurchasedByProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        // Lấy tất cả VariantEntity của product
        List<VariantEntity> variants = variantRepository.findByProductId(productId);
        if (variants.isEmpty()) {
            return List.of();
        }

        // Lấy tất cả VariantSizeEntity của các variants
        List<String> variantIds = variants.stream()
                .map(VariantEntity::getId)
                .toList();
        
        List<VariantSizeEntity> variantSizes = variantSizeRepository.findByVariantIdIn(variantIds);
        List<String> variantSizeIds = variantSizes.stream()
                .map(VariantSizeEntity::getId)
                .toList();

        if (variantSizeIds.isEmpty()) {
            return List.of();
        }

        // Lấy tất cả UserVariantEntity có variantSizeId trong danh sách
        List<UserVariantEntity> userVariants = userVariantRepository.findByVariantSizeIdIn(variantSizeIds);

        // Map sang response
        Map<String, VariantSizeEntity> variantSizeMap = variantSizes.stream()
                .collect(Collectors.toMap(VariantSizeEntity::getId, vs -> vs));

        Map<String, VariantEntity> variantMap = variants.stream()
                .collect(Collectors.toMap(VariantEntity::getId, v -> v));

        return userVariants.stream()
                .map(uv -> {
                    VariantSizeEntity variantSize = variantSizeMap.get(uv.getVariantSizeId());
                    if (variantSize == null) return null;

                    VariantEntity variant = variantMap.get(variantSize.getVariantId());
                    if (variant == null) return null;

                    ProductEntity product = productRepository.findById(variant.getProductId())
                            .orElse(null);
                    if (product == null) return null;

                    ProductGetResponse productDto = modelMapper.map(product, ProductGetResponse.class);
                    
                    VariantResponse variantDto = VariantResponse.builder()
                            .id(variantSize.getId())
                            .productId(variant.getProductId())
                            .color(variant.getColor())
                            .status(variant.getStatus())
                            .size(variantSize.getSize())
                            .stock(variantSize.getStock())
                            .countSell(variantSize.getCountSell())
                            .build();

                    return UserPurchasedItemResponse.builder()
                            .product(productDto)
                            .variant(variantDto)
                            .countBuy(uv.getQuantity())
                            .totalMoney(uv.getTotalPrice())
                            .userId(uv.getUserId())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
