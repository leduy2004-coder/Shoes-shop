package com.java.shoes_service.dto.payment;

import com.java.ProfileGetResponse;
import com.java.shoes_service.dto.product.product.OrderDetailResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentGetResponse {
    String paymentId;
    OrderDetailResponse response;

    ProfileGetResponse user;

}
