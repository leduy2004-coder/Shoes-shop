package com.java.shoes_service.controller;


import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.brand.BrandGetResponse;
import com.java.shoes_service.dto.brand.BrandRequest;
import com.java.shoes_service.service.BrandService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/brands")
public class BrandController {
    BrandService brandService;

    @GetMapping("/search")
    public ApiResponse<PageResponse<BrandGetResponse>> searchBrands(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, name = "sort_by") String sortBy,
            @RequestParam(required = false, name = "sort_order") String sortOrder
    ) {
        PageResponse<BrandGetResponse> response = brandService.searchBrands(
                page, size, search, sortBy, sortOrder
        );
        return ApiResponse.<PageResponse<BrandGetResponse>>builder()
                .result(response)
                .build();
    }

    @GetMapping("/detail/{id}")
    public ApiResponse<BrandGetResponse> getBrandById(@PathVariable String id) {
        BrandGetResponse response = brandService.getBrandById(id);
        return ApiResponse.<BrandGetResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping("/create")
    public ApiResponse<BrandGetResponse> create(
            @RequestPart("request") BrandRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file)
    {
        BrandGetResponse response = brandService.create(request, file);
        return ApiResponse.<BrandGetResponse>builder()
                .result(response)
                .build();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Boolean> deleteBrand(@PathVariable String id) {
        Boolean ok = brandService.delete(id);
        return ApiResponse.<Boolean>builder()
                .result(ok)
                .build();
    }
}