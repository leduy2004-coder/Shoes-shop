package com.java.shoes_service.dto.product.variant;

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
public class BuyVariantRequest {
    List<UserVariantRequest> items;
    String couponCode; // Mã giảm giá (có thể null)
    String addressId;
}

