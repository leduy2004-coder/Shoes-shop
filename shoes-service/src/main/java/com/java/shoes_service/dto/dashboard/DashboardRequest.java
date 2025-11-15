package com.java.shoes_service.dto.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardRequest {
    LocalDate fromDate; // có thể null
    LocalDate toDate;   // có thể null
}