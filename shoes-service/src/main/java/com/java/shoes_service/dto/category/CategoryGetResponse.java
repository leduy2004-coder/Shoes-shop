package com.java.shoes_service.dto.category;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryGetResponse {
    String id;
    String name;
    String description;
    long countProduct;
}
