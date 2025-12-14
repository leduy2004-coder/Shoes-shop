package com.java.shoes_service.controller.promotion;

import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.promotion.coupon.CouponGetResponse;
import com.java.shoes_service.dto.promotion.coupon.CouponRequest;
import com.java.shoes_service.dto.promotion.coupon.CouponResponse;
import com.java.shoes_service.service.promotion.CouponService;
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
@RequestMapping("/coupons")
public class CouponController {
    
    CouponService couponService;

    @GetMapping("/search")
    public ApiResponse<PageResponse<CouponGetResponse>> searchCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean active
    ) {
        PageResponse<CouponGetResponse> response = couponService.searchCoupons(page, size, code, active);
        return ApiResponse.<PageResponse<CouponGetResponse>>builder()
                .result(response)
                .build();
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<CouponGetResponse> getCouponById(@PathVariable String id) {
        CouponGetResponse response = couponService.getById(id);
        return ApiResponse.<CouponGetResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/get-by-code/{code}")
    public ApiResponse<CouponGetResponse> getCouponByCode(@PathVariable String code) {
        CouponGetResponse response = couponService.getByCode(code);
        return ApiResponse.<CouponGetResponse>builder()
                .result(response)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/save")
    public ApiResponse<CouponResponse> save(
            @RequestBody CouponRequest request
    ) {
        CouponResponse response = couponService.save(request);
        return ApiResponse.<CouponResponse>builder()
                .result(response)
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        Boolean deleted = couponService.delete(id);
        return ApiResponse.<Boolean>builder()
                .result(deleted)
                .build();
    }
}

