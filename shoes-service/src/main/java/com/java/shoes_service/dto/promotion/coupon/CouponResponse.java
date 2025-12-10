package com.java.shoes_service.dto.promotion.coupon;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CouponResponse {
    String id;
    String code;
    int discountPercent;
    double minOrder;
    int quantity;
    Instant expirationDate;
    boolean active;
    Instant createdDate;
    Instant modifiedDate;
}

