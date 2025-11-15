package com.java.shoes_service.service;


import com.java.shoes_service.dto.dashboard.DashboardRequest;
import com.java.shoes_service.dto.dashboard.DashboardResponse;
import com.java.shoes_service.dto.dashboard.MonthlyRevenueDto;
import com.java.shoes_service.dto.dashboard.RecentOrderDto;
import com.java.shoes_service.entity.product.UserVariantEntity;
import com.java.shoes_service.repository.product.ProductRepository;
import com.java.shoes_service.repository.product.UserVariantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardService {

    UserVariantRepository userVariantRepository;
    ProductRepository productRepository;

    public DashboardResponse getAdminDashboard(DashboardRequest request) {

        // 1. Xử lý khoảng ngày: nếu null -> mặc định 6 tháng gần nhất
        LocalDate toDate = Optional.ofNullable(request.getToDate())
                .orElse(LocalDate.now());
        LocalDate fromDate = Optional.ofNullable(request.getFromDate())
                .orElse(toDate.minusMonths(6));

        // convert sang Instant cho MongoDB query
        Instant fromInstant = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = toDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant(); // inclusive

        // 2. Lấy tất cả purchase trong khoảng
        List<UserVariantEntity> purchases = userVariantRepository.findByCreatedDateBetween(fromInstant, toInstant);

        // 3. Tổng orders = số record
        long totalOrders = purchases.size();

        // 4. Tổng doanh thu
        double totalRevenue = purchases.stream()
                .mapToDouble(p -> Optional.ofNullable(p.getTotalMoney()).orElse(0.0))
                .sum();

        // 5. Tổng user đã mua (distinct userId)
        long totalUsers = purchases.stream()
                .map(UserVariantEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        // 6. Tổng sản phẩm
        long totalProducts = productRepository.count();

        // 7. Doanh thu theo tháng (group by YearMonth)
        Map<YearMonth, Double> revenueMap = purchases.stream()
                .collect(Collectors.groupingBy(
                        p -> {
                            Instant createdAt = p.getCreatedDate(); // từ BaseEntity
                            LocalDateTime ldt = LocalDateTime.ofInstant(createdAt, ZoneId.systemDefault());
                            return YearMonth.from(ldt);
                        },
                        Collectors.summingDouble(p -> Optional.ofNullable(p.getTotalMoney()).orElse(0.0))
                ));

        List<MonthlyRevenueDto> revenueByMonth = revenueMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // sort theo thời gian
                .map(entry -> MonthlyRevenueDto.builder()
                        .year(entry.getKey().getYear())
                        .month(entry.getKey().getMonthValue())
                        .revenue(entry.getValue())
                        .build())
                .toList();

        // 8. Recent orders (10 đơn gần nhất trong khoảng)
        List<RecentOrderDto> recentOrders = purchases.stream()
                .sorted(Comparator.comparing(UserVariantEntity::getCreatedDate).reversed())
                .limit(10)
                .map(p -> RecentOrderDto.builder()
                        .id(p.getId())
                        .userId(p.getUserId())
                        .variantId(p.getVariantId())
                        .countBuy(p.getCountBuy())
                        .totalMoney(p.getTotalMoney())
                        .createdAt(p.getCreatedDate())
                        .build())
                .toList();

        return DashboardResponse.builder()
                .totalOrders(totalOrders)
                .totalUsers(totalUsers)
                .totalRevenue(totalRevenue)
                .totalProducts(totalProducts)
                .revenueByMonth(revenueByMonth)
                .recentOrders(recentOrders)
                .build();
    }
}
