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

    @PostMapping(value = "/upsert")
    public ApiResponse<List<VariantResponse>> upsertVariant(@RequestBody VariantCreateRequest request) {
        // Gộp create và update: Nếu variant (color + size) đã tồn tại thì giữ nguyên, nếu chưa thì tạo mới
        return ApiResponse.<List<VariantResponse>>builder()
                .result(variantService.upsertVariant(request))
                .build();
    }
    @PostMapping("/import-stock")
    public ApiResponse<List<VariantResponse>> importStocks(@RequestBody VariantStockImportListRequest req) {
        List<VariantResponse> res = variantService.importStocks(req);
        return ApiResponse.<List<VariantResponse>>builder().result(res).build();
    }
    @GetMapping("/history")
    public ApiResponse<PageResponse<VariantHistoryResponse>> history(
            @RequestParam(required = false) String variantSizeId, // ID của VariantSizeEntity
            @RequestParam(required = false) String productId,      // ID của ProductEntity
            @RequestParam(required = false) String variantId,       // ID của VariantEntity
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<VariantHistoryResponse> res = variantService.getHistory(variantSizeId, productId, variantId, name, page, size);
        return ApiResponse.<PageResponse<VariantHistoryResponse>>builder()
                .result(res)
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> deleteVariant(@PathVariable String id) {
        return ApiResponse.<Boolean>builder()
                .result(variantService.deleteVariant(id))
                .build();
    }

    @DeleteMapping("/delete-size/{variantSizeId}")
    public ApiResponse<Boolean> deleteVariantSize(@PathVariable String variantSizeId) {
        return ApiResponse.<Boolean>builder()
                .result(variantService.deleteVariantSize(variantSizeId))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<VariantResponse> getVariantById(@PathVariable String id) {
        VariantResponse response = variantService.getVariantById(id);
        return ApiResponse.<VariantResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/by-product/{productId}")
    public ApiResponse<List<VariantResponse>> getVariantsByProductId(@PathVariable String productId) {
        List<VariantResponse> response = variantService.getVariantsByProductId(productId);
        return ApiResponse.<List<VariantResponse>>builder()
                .result(response)
                .build();
    }

    @PostMapping("/buy")
    public ApiResponse<BuyVariantResponse> buyVariant(@RequestBody BuyVariantRequest request) {
        BuyVariantResponse response = userVariantService.buyVariantWithCoupon(request);
        return ApiResponse.<BuyVariantResponse>builder()
                .result(response)
                .build();
    }
}

