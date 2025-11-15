package com.java.shoes_service.dto.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecentOrderDto {
    String id;
    String userId;
    String variantId;
    long countBuy;
    Double totalMoney;
    Instant createdAt;
}