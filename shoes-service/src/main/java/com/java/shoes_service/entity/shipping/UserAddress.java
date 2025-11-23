package com.java.shoes_service.entity.shipping;

import com.java.shoes_service.entity.common.BaseEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "user_address")
public class UserAddress extends BaseEntity {
    String userId;

    Integer provinceCode;
    String provinceName;

    Integer districtCode;
    String districtName;

    Integer wardCode;
    String wardName;

    String addressLine;

    Boolean isDefault;
}
