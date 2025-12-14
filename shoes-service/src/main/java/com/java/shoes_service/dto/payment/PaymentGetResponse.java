package com.java.shoes_service.dto.payment;

import com.java.ProfileGetResponse;
import com.java.shoes_service.dto.product.product.ProductGetResponse;
import com.java.shoes_service.dto.product.variant.VariantResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentGetResponse {
    String id;
    String variantSizeId;
    String userId;
    String code;
    Integer amount;
    String bankCode;
    Instant expiryDate;

    ProfileGetResponse user;
    ProductGetResponse product;
    VariantResponse variant;
}
