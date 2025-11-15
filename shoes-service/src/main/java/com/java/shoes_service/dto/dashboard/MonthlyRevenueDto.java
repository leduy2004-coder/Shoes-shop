package com.java.shoes_service.dto.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthlyRevenueDto {
    int year;
    int month;
    double revenue;
}