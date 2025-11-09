package com.java.shoes_service.controller.promotion;


import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.promotion.banner.BannerRequest;
import com.java.shoes_service.dto.promotion.banner.BannerResponse;
import com.java.shoes_service.service.BannerService;
import com.java.shoes_service.utility.BannerSlot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/banners")
public class BannerController {
    BannerService bannerService;

    @GetMapping("/search")
    public ApiResponse<PageResponse<BannerResponse>> searchBanners(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title
    ) {
        PageResponse<BannerResponse> response = bannerService.searchBanner(page, size, title);
        return ApiResponse.<PageResponse<BannerResponse>>builder()
                .result(response)
                .build();
    }

    @GetMapping("/get-by-slot/{slot}")
    public ApiResponse<BannerResponse> getBannerBySlot(@PathVariable BannerSlot slot) {
        BannerResponse response = bannerService.getBannerBySlot(slot);
        return ApiResponse.<BannerResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping("/create-or-update")
    public ApiResponse<BannerResponse> create(
            @RequestPart("request") BannerRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file)
    {
        BannerResponse response = bannerService.createOrUpdate(request, file);
        return ApiResponse.<BannerResponse>builder()
                .result(response)
                .build();
    }
}