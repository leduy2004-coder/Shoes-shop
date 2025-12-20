package com.java.shoes_service.dto.brand;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BrandGetResponse {
    String id;
    String name;
    String logo;
    Instant createdDate;
    Instant modifiedDate;
    Integer countProduct;
}
