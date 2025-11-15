package com.java.shoes_service.dto.dashboard;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardResponse {

    long totalOrders;
    long totalUsers;
    double totalRevenue;
    long totalProducts;

    // theo tháng
    List<MonthlyRevenueDto> revenueByMonth;

    // đơn gần đây
    List<RecentOrderDto> recentOrders;
}