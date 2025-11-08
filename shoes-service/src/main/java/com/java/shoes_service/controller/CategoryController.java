package com.java.shoes_service.controller;


import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.category.CategoryGetResponse;
import com.java.shoes_service.dto.category.CategoryRequest;
import com.java.shoes_service.dto.category.CategoryResponse;
import com.java.shoes_service.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/categories")
public class CategoryController {
    CategoryService categoryService;


    @GetMapping
    public ApiResponse<List<CategoryGetResponse>> getAllCategories() {
        List<CategoryGetResponse> data = categoryService.getParentsWithChildren();
        return ApiResponse.<List<CategoryGetResponse>>builder()
                .result(data)
                .build();
    }

    @GetMapping("/{parentId}")
    public ApiResponse<CategoryGetResponse> getListCategory(@PathVariable String parentId) {
        CategoryGetResponse data = categoryService.getChildrenByParentId(parentId);
        return ApiResponse.<CategoryGetResponse>builder()
                .result(data)
                .build();
    }

    @GetMapping("/get-all-parent")
    public ApiResponse<List<CategoryResponse>> getAllParentCategories() {
        List<CategoryResponse> data = categoryService.getOnlyParents();
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(data)
                .build();
    }

    @PostMapping("/create")
    public ApiResponse<CategoryResponse> create(@RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ApiResponse.<CategoryResponse>builder()
                .result(response)
                .build();
    }


}