package com.java.shoes_service.dto.product.product;

import com.java.shoes_service.entity.product.VariantEntity;
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

    List<VariantEntity> variants;

}

