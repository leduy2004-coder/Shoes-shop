package com.java.shoes_service.dto.product.variant;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantStockImportListRequest {
    String productId;
    List<VariantStockImportItem> items;
}