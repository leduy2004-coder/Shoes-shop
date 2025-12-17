package com.java.shoes_service.entity.order;

import com.java.shoes_service.entity.common.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "purchase_order")
public class PurchaseOrderEntity extends BaseEntity {
    String userId;
    List<String> variantIds; // Lưu variantSizeIds
    double totalPrice; // Tổng giá trước khi giảm
    double finishPrice; // Giá sau khi áp dụng coupon
    String couponCode; // Mã coupon đã sử dụng (nếu có)
    Integer discountPercent; // Phần trăm giảm giá từ coupon (nếu có)
    String addressId; // Địa chỉ giao hàng
}

