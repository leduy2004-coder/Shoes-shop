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
@Document(value = "brand_image")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BrandImageEntity extends BaseEntity{
    String brandId;
    String name;
    String url;
}