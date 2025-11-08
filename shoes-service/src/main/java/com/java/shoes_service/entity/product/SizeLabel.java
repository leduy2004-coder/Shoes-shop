package com.java.shoes_service.entity.product;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SizeLabel {
    @Field("id")
    String id;

    @Field("label")
    String label;     // ví dụ: "38", "39", "M", "L"
}