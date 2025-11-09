package com.java.shoes_service.entity.cart;

import com.java.shoes_service.entity.common.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "cart")
@Getter
@Setter
public class CartEntity extends BaseEntity {
    String userId;
    int count;
    Double totalPrice;
}
