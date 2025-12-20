package com.java.shoes_service.dto.product.product;

import com.java.shoes_service.entity.payment.PaymentEntity;
import com.java.shoes_service.entity.shipping.UserAddress;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDetailResponse {
    String id;
    String userId;
    double totalPrice; // Tổng giá trước khi giảm
    double finishPrice; // Giá sau khi áp dụng coupon
    String couponCode; // Mã coupon đã sử dụng (nếu có)
    Integer discountPercent; // Phần trăm giảm giá từ coupon (nếu có)
    String addressId; // Địa chỉ giao hàng
    Instant createdDate;
    Instant modifiedDate;

    // Chi tiết
    UserAddress address; // Địa chỉ giao hàng
    PaymentEntity payment; // Thông tin thanh toán
    List<UserPurchasedItemResponse> items; // Danh sách sản phẩm trong đơn
}
