package com.java.shoes_service.dto.product.variant;

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
public class BuyVariantResponse {
    List<UserVariantResponse> items;
    String orderId;
}

