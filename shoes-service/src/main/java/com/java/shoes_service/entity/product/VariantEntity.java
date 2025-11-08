package com.java.shoes_service.entity.product;

import com.java.shoes_service.entity.common.BaseEntity;
import com.java.shoes_service.utility.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "variant")
@Getter
@Setter
public class VariantEntity extends BaseEntity {

    String productId;

    int stock;
    String color;
    ProductStatus status; // active | inactive

    int countSell;

    @Field("size")
    SizeLabel size;
}
