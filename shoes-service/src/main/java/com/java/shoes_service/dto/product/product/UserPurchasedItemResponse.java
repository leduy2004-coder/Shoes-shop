package com.java.shoes_service.dto.product.product;

import com.java.shoes_service.dto.product.variant.VariantResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPurchasedItemResponse {
    ProductGetResponse product;
    VariantResponse variant;
    long countBuy;
    Double totalMoney;
}
