package com.java.shoes_service.dto.product.product;

import com.java.ProfileGetResponse;
import com.java.shoes_service.dto.product.variant.VariantResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserOrderResponse {
    String orderId;
    double totalPrice;
    Integer discountPercent;
    double finishPrice;
    long countBuy;
    Double totalMoney;
    ProfileGetResponse user;
}
