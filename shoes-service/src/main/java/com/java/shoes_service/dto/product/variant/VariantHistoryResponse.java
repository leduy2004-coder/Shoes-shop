package com.java.shoes_service.dto.product.variant;

import com.java.shoes_service.dto.product.product.ProductGetResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantHistoryResponse {
    String id;
    ProductGetResponse product;
    String color;
    String size;
    int count;
    VariantResponse variant;
}

