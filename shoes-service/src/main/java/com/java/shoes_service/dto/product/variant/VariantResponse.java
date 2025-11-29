package com.java.shoes_service.dto.product.variant;

import com.java.shoes_service.utility.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantResponse {
    String id;
    String productId;
    int stock;
    String color;
    ProductStatus status;
    int countSell;
    String size;
}

