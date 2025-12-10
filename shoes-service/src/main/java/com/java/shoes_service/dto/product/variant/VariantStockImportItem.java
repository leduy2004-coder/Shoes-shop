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
public class VariantStockImportItem {
    String variantSizeId; // ID của VariantSizeEntity
    int count;
}
