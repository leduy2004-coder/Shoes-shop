package com.java.shoes_service.dto.product.variant;

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
public class VariantRequest {
    String id; // variantId - Nếu có thì UPDATE variant này, không có thì CREATE variant mới
    String color;
    List<VariantSizeRequest> sizes; // Mỗi size có thể có id đi kèm (variantSizeId) để update
}

