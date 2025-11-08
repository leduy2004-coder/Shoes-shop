package com.java.shoes_service.dto.product.product;

import com.java.shoes_service.dto.brand.BrandGetResponse;
import com.java.shoes_service.dto.category.CategoryResponse;
import com.java.shoes_service.utility.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductGetResponse {
    String id;
    CategoryResponse category;
    BrandGetResponse brand;
    String name;
    String slug;
    String description;
    double price;
    double discount;
    int totalStock;
    ProductStatus status;

    Instant createdDate;
    int averageRating;
    Instant modifiedDate;


}

