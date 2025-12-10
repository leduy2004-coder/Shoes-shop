package com.java.shoes_service.dto.product.product;

import com.java.CloudinaryResponse;
import com.java.shoes_service.dto.product.variant.VariantGroupResponse;
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
public class ProductGetDetailResponse {
    ProductGetResponse product;

    List<VariantGroupResponse> variants; // Grouped by variantId và color, mỗi variant có list sizes

    List<CloudinaryResponse> listImg;

}

