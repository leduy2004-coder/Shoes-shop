package com.java.shoes_service.controller;

import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.dashboard.DashboardRequest;
import com.java.shoes_service.dto.dashboard.DashboardResponse;
import com.java.shoes_service.service.DashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    DashboardService dashboardService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<DashboardResponse> getDashboard(@RequestBody DashboardRequest request) {
        DashboardResponse data = dashboardService.getAdminDashboard(request);
        return ApiResponse.<DashboardResponse>builder()
                .result(data)
                .build();
    }
}