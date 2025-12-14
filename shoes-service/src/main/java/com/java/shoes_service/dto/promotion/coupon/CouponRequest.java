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
public class CouponRequest {
    String id;
    String code;
    int discountPercent; // Phần trăm giảm giá
    double minOrder; // Giá trị đơn hàng tối thiểu
    int quantity; // Số lượng mã giảm giá
    Instant expirationDate; // Thời hạn hết hạn
    boolean active; // Trạng thái: active | inactive
}

