package com.java.shoes_service.dto.payment;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {
    String code;
    String message;
    String paymentUrl;
    String variantSizeId;
    long amount;
    String bankCode;
    String userId;
}
