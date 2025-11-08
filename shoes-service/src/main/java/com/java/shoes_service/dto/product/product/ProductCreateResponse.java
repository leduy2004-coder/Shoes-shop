package com.java.shoes_service.dto.product.product;

import com.java.CloudinaryResponse;
import com.java.shoes_service.utility.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductCreateResponse {
    String id;
    String name;
    String slug;
    String description;
    double price;
    double discount;
    int stock;
    ProductStatus status;

    Instant createdDate;

    Instant modifiedDate;

    List<CloudinaryResponse> imgUrls;

}
