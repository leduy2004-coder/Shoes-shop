package com.java.shoes_service.entity.product;

import com.java.shoes_service.entity.common.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "product_history")
@Getter
@Setter
public class HistoryProductEntity extends BaseEntity {
    String variantId;
    int count;
}
