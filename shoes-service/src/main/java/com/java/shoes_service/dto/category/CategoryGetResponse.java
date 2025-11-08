package com.java.shoes_service.dto.category;

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
public class CategoryGetResponse {
    String id;
    String name;
    String description;
    String parentId;

    List<CategoryResponse> children;
}
