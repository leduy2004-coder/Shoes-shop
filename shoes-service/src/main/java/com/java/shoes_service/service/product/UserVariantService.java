package com.java.shoes_service.service.product;


import com.java.shoes_service.dto.product.product.ProductGetResponse;
import com.java.shoes_service.dto.product.product.UserPurchasedItemResponse;
import com.java.shoes_service.dto.product.variant.UserVariantRequest;
import com.java.shoes_service.dto.product.variant.UserVariantResponse;
import com.java.shoes_service.dto.product.variant.VariantResponse;
import com.java.shoes_service.entity.product.ProductEntity;
import com.java.shoes_service.entity.product.UserVariantEntity;
import com.java.shoes_service.entity.product.VariantEntity;
import com.java.shoes_service.repository.product.ProductRepository;
import com.java.shoes_service.repository.product.UserVariantRepository;
import com.java.shoes_service.repository.product.VariantRepository;
import com.java.shoes_service.utility.GetInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserVariantService {

    VariantRepository variantRepository;
    ProductRepository productRepository;
    UserVariantRepository userVariantRepository;
    ModelMapper modelMapper;

    @Transactional
    public List<UserVariantResponse> buyVariant(List<UserVariantRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new RuntimeException("Request list is empty");
        }

        String userId = GetInfo.getLoggedInUserName();

        return requests.stream()
                .map(req -> {

                    // 1. Lấy variant
                    VariantEntity variant = variantRepository.findById(req.getVariantId())
                            .orElseThrow(() -> new RuntimeException("Variant not found: " + req.getVariantId()));

                    // 2. Lấy product từ productId trong variant
                    ProductEntity product = productRepository.findById(variant.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found: " + variant.getProductId()));

                    long countBuy = req.getCountBuy();
                    if (countBuy <= 0) {
                        throw new RuntimeException("countBuy must be > 0");
                    }

                    // 3. Check tồn kho của variant
                    if (variant.getStock() < countBuy) {
                        throw new RuntimeException("Not enough stock for variant: " + req.getVariantId());
                    }

                    // 4. Tiền dùng từ FE gửi lên (đã tính giảm giá)
                    double totalMoney = req.getTotalPrice() != null ? req.getTotalPrice() : 0.0;

                    // 5. Cập nhật Variant
                    variant.setStock(variant.getStock() - (int) countBuy);
                    variant.setCountSell(variant.getCountSell() + (int) countBuy);
                    variantRepository.save(variant);

                    // 6. Cập nhật Product
                    product.setTotalStock(product.getTotalStock() - (int) countBuy);
                    product.setCountSell(product.getCountSell() + (int) countBuy);
                    productRepository.save(product);

                    // 7. Lưu UserVariantEntity
                    UserVariantEntity userVariant = UserVariantEntity.builder()
                            .userId(userId)
                            .variantId(variant.getId())
                            .countBuy(countBuy)
                            .totalMoney(totalMoney)
                            .build();

                    userVariant = userVariantRepository.save(userVariant);

                    // 8. Map sang response cho từng item
                    return UserVariantResponse.builder()
                            .id(userVariant.getId())
                            .variantId(userVariant.getVariantId())
                            .countBuy(userVariant.getCountBuy())
                            .totalMoney(userVariant.getTotalMoney())
                            .build();
                })
                .toList();
    }


    public List<UserPurchasedItemResponse> getPurchasedByUser(String userId) {
        List<UserVariantEntity> userVariants = userVariantRepository.findByUserId(userId);

        return userVariants.stream()
                .map(uv -> {
                    VariantEntity variant = variantRepository.findById(uv.getVariantId())
                            .orElse(null);
                    if (variant == null) {
                        log.warn("Variant not found for id={}", uv.getVariantId());
                        return null;
                    }

                    ProductEntity product = productRepository.findById(variant.getProductId())
                            .orElse(null);
                    if (product == null) {
                        log.warn("Product not found for id={}", variant.getProductId());
                        return null;
                    }

                    ProductGetResponse productDto = modelMapper.map(product, ProductGetResponse.class);
                    VariantResponse variantDto = modelMapper.map(variant, VariantResponse.class);

                    return UserPurchasedItemResponse.builder()
                            .product(productDto)
                            .variant(variantDto)
                            .countBuy(uv.getCountBuy())
                            .totalMoney(uv.getTotalMoney())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
