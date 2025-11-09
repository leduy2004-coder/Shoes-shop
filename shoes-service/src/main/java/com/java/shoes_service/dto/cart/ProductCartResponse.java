package com.java.shoes_service.dto.cart;

import com.java.CloudinaryResponse;
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
public class ProductCartResponse {
    String id;

    String name;
    String slug;
    String description;
    double price;
    double discount;

    int averageRating;

    CloudinaryResponse imageUrl;
}

