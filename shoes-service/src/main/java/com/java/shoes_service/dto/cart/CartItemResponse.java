package com.java.shoes_service.dto.cart;

import com.java.shoes_service.entity.cart.Variant;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    String id;
    String cartId;
    int quantity;
    Variant variant;
    ProductCartResponse product;
}

