package com.java.shoes_service.dto.product.product;

import com.java.shoes_service.dto.product.variant.VariantCreateRequest;
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
public class ProductCreateRequest {
    String categoryId;
    String brandId;
    String name;
    String slug;
    String description;
    double price;
    double discount;

    List<VariantCreateRequest> variants;
}

