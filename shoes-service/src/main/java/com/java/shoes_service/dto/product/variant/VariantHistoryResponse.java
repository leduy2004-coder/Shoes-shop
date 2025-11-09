package com.java.shoes_service.dto.product.variant;

import com.java.shoes_service.dto.product.product.ProductGetResponse;
import com.java.shoes_service.entity.product.SizeLabel;
import com.java.shoes_service.utility.ProductStatus;
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
    SizeLabel size;
    int count;
    VariantResponse variant;
}

