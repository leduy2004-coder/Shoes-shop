package com.java.shoes_service.entity.cart;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Variant {
    @Field("id")
    String id;

    String productId;
    String color;
    String sizeLabel;
    int stock;
}