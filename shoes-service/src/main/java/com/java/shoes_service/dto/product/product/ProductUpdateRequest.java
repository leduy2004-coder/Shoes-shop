package com.java.shoes_service.dto.product.product;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateRequest {
    String productId;
    String categoryId;
    String brandId;
    String name;
    String slug;
    String description;
    double price;
    double discount;
}

