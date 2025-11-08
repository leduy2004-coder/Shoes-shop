package com.java.shoes_service.dto.product;

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
    Category category;
    Brand brand;
    String name;
    String slug;
    String description;
    double price;
    double discount;
    int stock;
    ProductStatus status;

    Instant createdDate;
    int averageRating;
    Instant modifiedDate;


    private static class Category {
        String id;
        String name;
    }
    private static class Brand {
        String id;
        String name;
    }

}

