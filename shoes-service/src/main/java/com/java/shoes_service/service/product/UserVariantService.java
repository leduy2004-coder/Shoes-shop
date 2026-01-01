package com.java.shoes_service.service.product;


import com.java.CloudinaryResponse;
import com.java.ImageType;
import com.java.ProfileGetResponse;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.product.product.*;
import com.java.shoes_service.dto.product.variant.*;
import com.java.shoes_service.entity.order.PurchaseOrderEntity;
import com.java.shoes_service.entity.order.UserVariantEntity;
import com.java.shoes_service.entity.payment.PaymentEntity;
import com.java.shoes_service.entity.product.HistoryProductEntity;
import com.java.shoes_service.entity.product.ProductEntity;
import com.java.shoes_service.entity.product.VariantEntity;
import com.java.shoes_service.entity.product.VariantSizeEntity;
import com.java.shoes_service.entity.promotion.CouponEntity;
import com.java.shoes_service.entity.shipping.UserAddress;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.UserAddressRepository;
import com.java.shoes_service.repository.httpClient.FileClient;
import com.java.shoes_service.repository.httpClient.ProfileClient;
import com.java.shoes_service.repository.order.PurchaseOrderRepository;
import com.java.shoes_service.repository.payment.PaymentOrderRepository;
import com.java.shoes_service.repository.product.*;
import com.java.shoes_service.repository.promotion.CouponRepository;
import com.java.shoes_service.utility.GetInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
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
    PaymentOrderRepository paymentOrderRepository;
    CouponRepository couponRepository;
    HistoryProductRepository historyProductRepository;
    UserAddressRepository userAddressRepository;
    ProfileClient profileClient;
    FileClient fileClient;
    ModelMapper modelMapper;

    @Transactional
    public BuyVariantResponse buyVariantWithCoupon(BuyVariantRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        String userId = GetInfo.getLoggedInUserName();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        List<UserVariantRequest> items = request.getItems();
        String couponCode = request.getCouponCode();
        String addressId = request.getAddressId();
        
        // Validate addressId
        if (addressId == null || addressId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
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
            // Get variantSize từ database
            VariantSizeEntity variantSize = variantSizeRepository.findById(item.getVariantSizeId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            
            // Get variant từ database
            VariantEntity variant = variantRepository.findById(variantSize.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            
            // Get product từ database để lấy giá và discount mới nhất
            ProductEntity product = productRepository.findById(variant.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
            
            long quantity = item.getQuantity();
            if (quantity <= 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            
            if (variantSize.getStock() < quantity) {
                throw new AppException(ErrorCode.EXCEED_STOCK);
            }
            
            // Tự động tính giá sau giảm giá theo product
            double effectivePrice = calculateEffectivePrice(product);
            
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

        // 7. Lưu PurchaseOrderEntity (variantIds giờ là variantSizeIds)
        PurchaseOrderEntity purchaseOrder = PurchaseOrderEntity.builder()
                .userId(userId)
                .variantIds(variantSizeIds) // Lưu variantSizeIds
                .totalPrice(totalPrice)
                .finishPrice(finishPrice)
                .couponCode(couponCode)
                .discountPercent(discountPercent)
                .addressId(addressId) // Lưu addressId
                .status(false)
                .build();

        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        for (UserVariantRequest item : items) {
            VariantSizeEntity variantSize = variantSizeRepository.findById(item.getVariantSizeId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            
            VariantEntity variant = variantRepository.findById(variantSize.getVariantId())
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
            
            ProductEntity product = productRepository.findById(variant.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
            
            long quantity = item.getQuantity();
            
            // Tính giá cho variant này (phân bổ từ finishPrice)
            // Tự động get product và tính giá sau giảm giá
            double itemPrice = 0.0;
            if (items.size() == 1) {
                itemPrice = finishPrice;
            } else {
                // Tính tỷ lệ giá của variant này so với tổng
                // Tự động tính giá sau giảm giá từ product
                double effectivePrice = calculateEffectivePrice(product);
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
                    .orderId(purchaseOrder.getId())
                    .variantSizeId(variantSize.getId())
                    .quantity(quantity)
                    .status(false)
                    .totalPrice(itemPrice)
                    .build();
            
            userVariant = userVariantRepository.save(userVariant);
            
            responses.add(UserVariantResponse.builder()
                    .id(userVariant.getId())
                    .variantId(userVariant.getVariantSizeId())
                    .quantity(userVariant.getQuantity())
                    .totalMoney(userVariant.getTotalPrice())
                    .build());
        }

        // 6. Cập nhật quantity của coupon nếu có
        if (coupon != null) {
            coupon.setQuantity(coupon.getQuantity() - 1);
            couponRepository.save(coupon);
        }
        return BuyVariantResponse.builder().items(responses).orderId(purchaseOrder.getId()).build();
    }

    public PageResponse<UserPurchasedItemResponse> getPurchasedByUser(String userId, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), sort);
        
        Page<UserVariantEntity> userVariantsPage = userVariantRepository.findByUserId(userId, pageable);
        List<UserVariantEntity> userVariants = userVariantsPage.getContent();

        List<UserPurchasedItemResponse> items = userVariants.stream()
                .map(uv -> {
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
                            .id(uv.getId())
                            .product(productDto)
                            .variant(variantDto)
                            .countBuy(uv.getQuantity())
                            .totalMoney(uv.getTotalPrice())
                            .userId(uv.getUserId())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        // Batch load images for all products to optimize performance
        Map<String, CloudinaryResponse> imageCache = batchLoadProductImages(
                items.stream()
                        .map(item -> item.getProduct() != null ? item.getProduct().getId() : null)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList())
        );

        // Set image URLs
        items.forEach(item -> {
            if (item.getProduct() != null && item.getProduct().getId() != null) {
                item.getProduct().setImageUrl(imageCache.get(item.getProduct().getId()));
            }
        });

        return new PageResponse<>(
                page,
                userVariantsPage.getSize(),
                userVariantsPage.getTotalElements(),
                userVariantsPage.getTotalPages(),
                items
        );
    }

    public PageResponse<UserPurchasedOrderResponse> getPurchasedByUserFromToken(int page, int size) {
        String userId = GetInfo.getLoggedInUserName();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return getPurchasedOrdersByUser(userId, page, size);
    }

    public PageResponse<UserPurchasedOrderResponse> getPurchasedOrdersByUser(String userId, int page, int size) {
        // Phân trang PurchaseOrderEntity của user
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), sort);
        Page<PurchaseOrderEntity> ordersPage = purchaseOrderRepository.findByUserIdAndStatus(userId,true, pageable);
        List<PurchaseOrderEntity> orders = ordersPage.getContent();

        if (orders.isEmpty()) {
            return new PageResponse<>(page, size, 0, 0, List.of());
        }

        // Lấy tất cả orderIds
        List<String> orderIds = orders.stream()
                .map(PurchaseOrderEntity::getId)
                .toList();

        // Lấy tất cả UserVariantEntity từ các orders
        Map<String, List<UserVariantEntity>> orderItemsMap = new java.util.HashMap<>();
        for (String orderId : orderIds) {
            List<UserVariantEntity> userVariants = userVariantRepository.findByOrderId(orderId);
            orderItemsMap.put(orderId, userVariants);
        }

        // Lấy tất cả variantSizeIds từ tất cả items
        List<String> variantSizeIds = orderItemsMap.values().stream()
                .flatMap(List::stream)
                .map(UserVariantEntity::getVariantSizeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // Batch load variant sizes
        Map<String, VariantSizeEntity> variantSizeMap = variantSizeIds.isEmpty()
                ? Map.of()
                : variantSizeRepository.findAllById(variantSizeIds).stream()
                .collect(Collectors.toMap(VariantSizeEntity::getId, Function.identity()));

        // Batch load variants
        List<String> variantIds = variantSizeMap.values().stream()
                .map(VariantSizeEntity::getVariantId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, VariantEntity> variantMap = variantIds.isEmpty()
                ? Map.of()
                : variantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(VariantEntity::getId, Function.identity()));

        // Batch load products
        List<String> productIds = variantMap.values().stream()
                .map(VariantEntity::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, ProductEntity> productMap = productIds.isEmpty()
                ? Map.of()
                : productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        // Load user profile
        ProfileGetResponse user = null;
        try {
            user = profileClient.getProfile(userId).getResult();
        } catch (Exception e) {
            log.warn("Could not fetch profile for userId: {}", userId, e);
        }

        // Batch load images for all products
        Map<String, CloudinaryResponse> imageCache = batchLoadProductImages(productIds);

        // Map orders to UserPurchasedOrderResponse
        ProfileGetResponse finalUser = user;
        List<UserPurchasedOrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    List<UserVariantEntity> userVariants = orderItemsMap.get(order.getId());
                    if (userVariants == null || userVariants.isEmpty()) {
                        return null;
                    }

                    // Map items to ItemOrderResponse
                    List<ItemOrderResponse> items = userVariants.stream()
                            .map(uv -> {
                                VariantSizeEntity variantSize = variantSizeMap.get(uv.getVariantSizeId());
                                if (variantSize == null) return null;

                                VariantEntity variant = variantMap.get(variantSize.getVariantId());
                                if (variant == null) return null;

                                ProductEntity product = productMap.get(variant.getProductId());
                                if (product == null) return null;

                                ProductGetResponse productDto = modelMapper.map(product, ProductGetResponse.class);
                                
                                // Set image URL
                                CloudinaryResponse image = imageCache.get(product.getId());
                                if (image != null) {
                                    productDto.setImageUrl(image);
                                }

                                VariantResponse variantDto = VariantResponse.builder()
                                        .id(variantSize.getId())
                                        .productId(variant.getProductId())
                                        .color(variant.getColor())
                                        .status(variant.getStatus())
                                        .size(variantSize.getSize())
                                        .stock(variantSize.getStock())
                                        .countSell(variantSize.getCountSell())
                                        .build();

                                return ItemOrderResponse.builder()
                                        .id(uv.getId())
                                        .product(productDto)
                                        .variant(variantDto)
                                        .countBuy(uv.getQuantity())
                                        .totalMoney(uv.getTotalPrice())
                                        .build();
                            })
                            .filter(Objects::nonNull)
                            .toList();

                    if (items.isEmpty()) {
                        return null;
                    }

                    return UserPurchasedOrderResponse.builder()
                            .listPurchase(items)
                            .userId(order.getUserId())
                            .orderId(order.getId())
                            .totalPrice(order.getTotalPrice())
                            .discountPercent(order.getDiscountPercent())
                            .addressId(order.getAddressId())
                            .finishPrice(order.getFinishPrice())
                            .user(finalUser)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        return new PageResponse<>(
                page,
                ordersPage.getSize(),
                ordersPage.getTotalElements(),
                ordersPage.getTotalPages(),
                orderResponses
        );
    }

    public PageResponse<UserPurchasedItemResponse> getPurchasedByProductId(String productId, int page, int size) {
        if (productId == null || productId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        // Lấy tất cả VariantEntity của product
        List<VariantEntity> variants = variantRepository.findByProductId(productId);
        if (variants.isEmpty()) {
            return new PageResponse<>(page, size, 0, 0, List.of());
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
            return new PageResponse<>(page, size, 0, 0, List.of());
        }

        // Phân trang UserVariantEntity có variantSizeId trong danh sách
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), sort);
        Page<UserVariantEntity> userVariantsPage = userVariantRepository.findByVariantSizeIdInAndStatus(variantSizeIds,true, pageable);
        List<UserVariantEntity> userVariants = userVariantsPage.getContent();

        // Batch load user profiles
        List<String> userIds = userVariants.stream()
                .map(UserVariantEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, ProfileGetResponse> userMap = userIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        userId -> {
                            try {
                                return profileClient.getProfile(userId).getResult();
                            } catch (Exception e) {
                                log.warn("Could not fetch profile for userId: {}", userId, e);
                                return null;
                            }
                        }
                ));

        // Map sang response
        Map<String, VariantSizeEntity> variantSizeMap = variantSizes.stream()
                .collect(Collectors.toMap(VariantSizeEntity::getId, Function.identity()));

        Map<String, VariantEntity> variantMap = variants.stream()
                .collect(Collectors.toMap(VariantEntity::getId, Function.identity()));

        List<UserPurchasedItemResponse> items = userVariants.stream()
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

                    // Lấy thông tin user
                    ProfileGetResponse user = userMap.get(uv.getUserId());

                    return UserPurchasedItemResponse.builder()
                            .id(uv.getId())
                            .product(productDto)
                            .variant(variantDto)
                            .countBuy(uv.getQuantity())
                            .totalMoney(uv.getTotalPrice())
                            .userId(uv.getUserId())
                            .user(user)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        // Batch load images for all products to optimize performance
        Map<String, CloudinaryResponse> imageCache = batchLoadProductImages(
                items.stream()
                        .map(item -> item.getProduct() != null ? item.getProduct().getId() : null)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList())
        );

        // Set image URLs
        items.forEach(item -> {
            if (item.getProduct() != null && item.getProduct().getId() != null) {
                item.getProduct().setImageUrl(imageCache.get(item.getProduct().getId()));
            }
        });

        return new PageResponse<>(
                page,
                userVariantsPage.getSize(),
                userVariantsPage.getTotalElements(),
                userVariantsPage.getTotalPages(),
                items
        );
    }

    public OrderDetailResponse getOrderDetail(String userVariantId) {
        String currentUserId = GetInfo.getLoggedInUserName();
        boolean isAdmin = GetInfo.isAdmin();

        // Lấy UserVariantEntity từ userVariantId
        UserVariantEntity userVariant = userVariantRepository.findById(userVariantId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

        // Kiểm tra quyền: user chỉ xem được order của mình, admin xem được tất cả
        if (!isAdmin && !userVariant.getUserId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Lấy orderId từ UserVariantEntity
        String orderId = userVariant.getOrderId();
        if (orderId == null || orderId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Lấy PurchaseOrderEntity
        PurchaseOrderEntity order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

        // Lấy address
        UserAddress address = null;
        if (order.getAddressId() != null) {
            address = userAddressRepository.findById(order.getAddressId()).orElse(null);
        }

        // Lấy payment
        PaymentEntity payment = paymentOrderRepository.findByOrderId(orderId).orElse(null);

        // Lấy danh sách items (UserVariantEntity)
        List<UserVariantEntity> userVariants = userVariantRepository.findByOrderId(orderId);

        // Batch load để map items
        List<String> variantSizeIds = userVariants.stream()
                .map(UserVariantEntity::getVariantSizeId)
                .distinct()
                .toList();

        Map<String, VariantSizeEntity> variantSizeMap = variantSizeRepository.findAllById(variantSizeIds).stream()
                .collect(Collectors.toMap(VariantSizeEntity::getId, Function.identity()));

        List<String> variantIds = variantSizeMap.values().stream()
                .map(VariantSizeEntity::getVariantId)
                .distinct()
                .toList();

        Map<String, VariantEntity> variantMap = variantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(VariantEntity::getId, Function.identity()));

        List<String> productIds = variantMap.values().stream()
                .map(VariantEntity::getProductId)
                .distinct()
                .toList();

        Map<String, ProductEntity> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        // Map items
        List<UserPurchasedItemResponse> items = userVariants.stream()
                .map(uv -> {
                    VariantSizeEntity variantSize = variantSizeMap.get(uv.getVariantSizeId());
                    if (variantSize == null) return null;

                    VariantEntity variant = variantMap.get(variantSize.getVariantId());
                    if (variant == null) return null;

                    ProductEntity product = productMap.get(variant.getProductId());
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
                            .id(uv.getId())
                            .product(productDto)
                            .variant(variantDto)
                            .countBuy(uv.getQuantity())
                            .totalMoney(uv.getTotalPrice())
                            .userId(uv.getUserId())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        // Batch load images for all products to optimize performance
        Map<String, CloudinaryResponse> imageCache = batchLoadProductImages(
                items.stream()
                        .map(item -> item.getProduct() != null ? item.getProduct().getId() : null)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList())
        );

        // Set image URLs
        items.forEach(item -> {
            if (item.getProduct() != null && item.getProduct().getId() != null) {
                item.getProduct().setImageUrl(imageCache.get(item.getProduct().getId()));
            }
        });

        return OrderDetailResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalPrice(order.getTotalPrice())
                .finishPrice(order.getFinishPrice())
                .couponCode(order.getCouponCode())
                .discountPercent(order.getDiscountPercent())
                .addressId(order.getAddressId())
                .createdDate(order.getCreatedDate())
                .modifiedDate(order.getModifiedDate())
                .address(address)
                .payment(payment)
                .items(items)
                .build();
    }

    public OrderDetailResponse getOrderDetailByPurchaseId(String purchaseId) {
        String currentUserId = GetInfo.getLoggedInUserName();
        boolean isAdmin = GetInfo.isAdmin();

        // Lấy PurchaseOrderEntity
        PurchaseOrderEntity order = purchaseOrderRepository.findById(purchaseId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

        // Kiểm tra quyền: user chỉ xem được order của mình, admin xem được tất cả
        if (!isAdmin && !order.getUserId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Lấy address
        UserAddress address = null;
        if (order.getAddressId() != null) {
            address = userAddressRepository.findById(order.getAddressId()).orElse(null);
        }

        // Lấy payment
        PaymentEntity payment = paymentOrderRepository.findByOrderId(purchaseId).orElse(null);

        // Lấy danh sách items (UserVariantEntity)
        List<UserVariantEntity> userVariants = userVariantRepository.findByOrderId(purchaseId);

        // Batch load để map items
        List<String> variantSizeIds = userVariants.stream()
                .map(UserVariantEntity::getVariantSizeId)
                .distinct()
                .toList();

        Map<String, VariantSizeEntity> variantSizeMap = variantSizeRepository.findAllById(variantSizeIds).stream()
                .collect(Collectors.toMap(VariantSizeEntity::getId, Function.identity()));

        List<String> variantIds = variantSizeMap.values().stream()
                .map(VariantSizeEntity::getVariantId)
                .distinct()
                .toList();

        Map<String, VariantEntity> variantMap = variantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(VariantEntity::getId, Function.identity()));

        List<String> productIds = variantMap.values().stream()
                .map(VariantEntity::getProductId)
                .distinct()
                .toList();

        Map<String, ProductEntity> productMap = productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));

        // Map items với đầy đủ product info
        List<UserPurchasedItemResponse> items = userVariants.stream()
                .map(uv -> {
                    VariantSizeEntity variantSize = variantSizeMap.get(uv.getVariantSizeId());
                    if (variantSize == null) return null;

                    VariantEntity variant = variantMap.get(variantSize.getVariantId());
                    if (variant == null) return null;

                    ProductEntity product = productMap.get(variant.getProductId());
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
                            .id(uv.getId())
                            .product(productDto)
                            .variant(variantDto)
                            .countBuy(uv.getQuantity())
                            .totalMoney(uv.getTotalPrice())
                            .userId(uv.getUserId())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        // Batch load images for all products to optimize performance
        Map<String, CloudinaryResponse> imageCache = batchLoadProductImages(
                items.stream()
                        .map(item -> item.getProduct() != null ? item.getProduct().getId() : null)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList())
        );

        // Set image URLs
        items.forEach(item -> {
            if (item.getProduct() != null && item.getProduct().getId() != null) {
                item.getProduct().setImageUrl(imageCache.get(item.getProduct().getId()));
            }
        });

        return OrderDetailResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalPrice(order.getTotalPrice())
                .finishPrice(order.getFinishPrice())
                .couponCode(order.getCouponCode())
                .discountPercent(order.getDiscountPercent())
                .addressId(order.getAddressId())
                .createdDate(order.getCreatedDate())
                .modifiedDate(order.getModifiedDate())
                .address(address)
                .payment(payment)
                .items(items)
                .build();
    }

    private double calculateEffectivePrice(ProductEntity product) {
        if (product == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }
        
        double productPrice = product.getPrice();
        double productDiscount = product.getDiscount();
        
        // Đảm bảo discount trong khoảng hợp lệ [0, 100]
        double discount = Math.max(0.0, Math.min(100.0, productDiscount));
        
        // Tính giá sau giảm giá
        return productPrice * (1 - discount / 100.0);
    }

    /**
     * Batch load product images to optimize performance and reduce delay
     * Loads primary images for multiple products at once using parallel processing
     */
    private Map<String, CloudinaryResponse> batchLoadProductImages(List<String> productIds) {
        Map<String, CloudinaryResponse> imageCache = new ConcurrentHashMap<>();
        
        if (productIds == null || productIds.isEmpty()) {
            return imageCache;
        }

        // Load images in parallel to reduce delay
        productIds.parallelStream().forEach(productId -> {
            try {
                var apiRes = fileClient.getImage(productId, ImageType.PRODUCT);
                var images = (apiRes != null) ? apiRes.getResult() : null;
                if (images != null && !images.isEmpty()) {
                    // Get primary image or first image
                    CloudinaryResponse primaryImage = images.stream()
                            .filter(img -> img != null && Boolean.TRUE.equals(img.getIsPrimary()))
                            .findFirst()
                            .orElse(images.get(0));
                    if (primaryImage != null) {
                        imageCache.put(productId, primaryImage);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch image for productId: {}", productId, e);
            }
        });

        return imageCache;
    }
}
