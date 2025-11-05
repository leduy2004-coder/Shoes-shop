package com.java.shoes_service.entity.product;

import com.java.shoes_service.entity.brand.BrandEntity;
import com.java.shoes_service.entity.brand.CategoryEntity;
import com.java.shoes_service.entity.common.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "products")
public class ProductEntity extends BaseEntity {
    @DBRef
    private CategoryEntity category;
    @DBRef
    private BrandEntity brand;

    private String name;
    private String slug;
    private String description;
    private double price;
    private double discount;
    private int stock;
    private String status; // active | inactive
}
