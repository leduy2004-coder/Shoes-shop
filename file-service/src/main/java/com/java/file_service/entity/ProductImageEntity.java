package com.java.file_service.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document(value = "product_image")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductImageEntity extends BaseEntity{
    String productId;
    String name;
    String url;
    Boolean primary;
}