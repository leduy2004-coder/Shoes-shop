package com.java.shoes_service.dto.product.variant;

import com.java.shoes_service.utility.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantGroupResponse {
    String id; // variantId
    String productId;
    String color;
    ProductStatus status;
    List<VariantSizeResponse> sizes; // Danh sách các size của variant này
}

