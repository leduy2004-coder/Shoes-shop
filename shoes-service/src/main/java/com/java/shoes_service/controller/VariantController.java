package com.java.shoes_service.controller;

import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.product.variant.*;
import com.java.shoes_service.service.product.UserVariantService;
import com.java.shoes_service.service.product.VariantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/variants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VariantController {
    VariantService variantService;
    UserVariantService userVariantService;

    @PostMapping(value = "/create-variant")
    public ApiResponse<List<VariantResponse>> createVariant(@RequestBody VariantCreateRequest request) {
        return ApiResponse.<List<VariantResponse>>builder()
                .result(variantService.createVariant(request))
                .build();
    }
    @PostMapping("/import-stock")
    public ApiResponse<List<VariantResponse>> importStocks(@RequestBody VariantStockImportListRequest req) {
        List<VariantResponse> res = variantService.importStocks(req);
        return ApiResponse.<List<VariantResponse>>builder().result(res).build();
    }
    @GetMapping("/history")
    public ApiResponse<PageResponse<VariantHistoryResponse>> history(
            @RequestParam(required = false)  String variantId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<VariantHistoryResponse> res = variantService.getHistory(variantId, page, size);
        return ApiResponse.<PageResponse<VariantHistoryResponse>>builder()
                .result(res)
                .build();
    }
    @PutMapping(value = "/update")
    public ApiResponse<VariantResponse> updateVariant(@RequestBody VariantUpdateRequest request) {
        return ApiResponse.<VariantResponse>builder()
                .result(variantService.updateVariant(request))
                .build();
    }
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> deleteVariant(@PathVariable String id) {
        return ApiResponse.<Boolean>builder()
                .result(variantService.deleteVariant(id))
                .build();
    }

    @PostMapping("/buy")
    public ApiResponse<List<UserVariantResponse>> buyVariant(@RequestBody List<UserVariantRequest> request) {
        List<UserVariantResponse> response = userVariantService.buyVariant(request);
        return ApiResponse.<List<UserVariantResponse>>builder()
                .result(response)
                .build();
    }
}

