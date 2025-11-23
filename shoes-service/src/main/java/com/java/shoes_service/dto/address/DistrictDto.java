package com.java.shoes_service.dto.address;

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
public class DistrictDto {
    int code;
    String name;
    String codename;
    String division_type;
    int province_code;
    List<WardDto> wards;

}