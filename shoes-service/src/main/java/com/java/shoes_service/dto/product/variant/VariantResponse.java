package com.java.shoes_service.dto.product.variant;

import com.java.shoes_service.entity.product.SizeLabel;
import com.java.shoes_service.utility.ProductStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantResponse {
    String id;
    String productId;
    int stock;
    String color;
    ProductStatus status;
    int countSell;
    SizeLabel size;
}

