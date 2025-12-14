package com.java.shoes_service.dto.product.variant;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantSizeRequest {
    String id; // variantSizeId - Nếu có thì UPDATE variantSize này, không có thì CREATE variantSize mới
    String size; // Size của variant (có thể là String như "38", "39", "L", "M", etc.)
    Integer stock; // Số lượng tồn kho (optional, có thể null)
}

