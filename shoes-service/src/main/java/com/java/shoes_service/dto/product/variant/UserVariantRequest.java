package com.java.shoes_service.dto.product.variant;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserVariantRequest {
    String variantId;
    long countBuy;
    Double totalPrice;
}