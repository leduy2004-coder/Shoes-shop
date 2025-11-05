package com.java.shoes_service.entity.order;

import com.java.shoes_service.entity.common.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "order")
public class OrderEntity extends BaseEntity {
    String userId;
    double totalAmount;
    String paymentMethod; // cod, momo, paypal...
    String paymentStatus; // pending, paid, failed
    String shippingMethod; // standard, express...
    double shippingFee;
    String shippingStatus;
    String trackingCode;
    String shippingAddress;
    String receiverName;
    String receiverPhone;
}
