package com.java.shoes_service.dto.address;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WardDto {
    int code;
    String name;
    String codename;
    String division_type;
    int district_code;
}