package com.java.shoes_service.entity.product;

import com.java.shoes_service.entity.common.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "variant_size")
@Getter
@Setter
public class VariantSizeEntity extends BaseEntity {
    String variantId;
    int stock;
    int countSell;
    String size;
}
