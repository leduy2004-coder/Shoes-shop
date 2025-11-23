package com.java.shoes_service.dto.address;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProvinceDto {
    int code;
    String name;
    String codename;
    String division_type;
    String phone_code;
    List<DistrictDto> districts;
}
