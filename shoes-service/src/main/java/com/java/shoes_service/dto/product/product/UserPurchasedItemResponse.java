package com.java.shoes_service.dto.product.product;

import com.java.shoes_service.dto.product.variant.VariantResponse;
import com.java.shoes_service.entity.shipping.UserAddress;
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
    String userId; // User ID - để admin biết ai đã mua sản phẩm
    UserAddress address;
}
