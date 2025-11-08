package com.java.shoes_service.entity.brand;

import com.java.shoes_service.entity.common.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "brands")
@Setter
@Getter
public class BrandEntity extends BaseEntity {
    String name;
    String logo;
}
