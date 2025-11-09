package com.java.shoes_service.dto.cart;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartGetResponse {
    String id;
    String userId;
    int count;
    Double totalPrice;
    List<CartItemResponse> items;
    Instant createdDate;
    Instant modifiedDate;
}

