package com.java.shoes_service.entity.product;

import com.java.shoes_service.entity.common.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "user_variant")
@Getter
@Setter
public class UserVariantEntity extends BaseEntity {
    String userId;
    String variantId;
    long countBuy;
    Double totalMoney;
}
