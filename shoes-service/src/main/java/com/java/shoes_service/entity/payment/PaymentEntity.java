package com.java.shoes_service.entity.payment;

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
@Document(collection = "payment")
public class PaymentEntity extends BaseEntity {
    String orderId;
    double amount;
    String method;
    String status;
    String transactionId;
}
