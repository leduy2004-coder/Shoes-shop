package com.java.shoes_service.dto.address;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAddressRequest {
    String userId;
    String addressId;

    Integer provinceCode;
    String provinceName;

    Integer districtCode;
    String districtName;

    Integer wardCode;
    String wardName;

    String addressLine;
    Boolean isDefault = false;

    String nameReceiver;
    String phoneReceiver;
}
