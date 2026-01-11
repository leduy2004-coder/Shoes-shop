package com.java.shoes_service.entity.order;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderAddress {
    Integer provinceCode;
    String provinceName;

    Integer districtCode;
    String districtName;

    Integer wardCode;
    String wardName;

    String addressLine;

    String nameReceiver;
    String phoneReceiver;
    String emailReceiver;
}

