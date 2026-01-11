package com.java.shoes_service.dto.cart;

import com.java.CloudinaryResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

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

