package com.java.shoes_service.controller;

import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.product.variant.VariantCreateRequest;
import com.java.shoes_service.dto.product.variant.VariantHistoryResponse;
import com.java.shoes_service.dto.product.variant.VariantResponse;
import com.java.shoes_service.dto.product.variant.VariantStockImportListRequest;
import com.java.shoes_service.service.product.VariantService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/variants")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VariantController {
    VariantService variantService;


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
}
