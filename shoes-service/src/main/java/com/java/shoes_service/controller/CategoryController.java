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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/categories")
public class CategoryController {
    CategoryService categoryService;

    @GetMapping("/get-all")
    public ApiResponse<List<CategoryGetResponse>> getAllCategories() {
        List<CategoryGetResponse> data = categoryService.getAll();
        return ApiResponse.<List<CategoryGetResponse>>builder()
                .result(data)
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/create")
    public ApiResponse<CategoryResponse> create(@RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ApiResponse.<CategoryResponse>builder()
                .result(response)
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/update/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable String id,
                                                @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.update(id, request);
        return ApiResponse.<CategoryResponse>builder()
                .result(response)
                .build();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        Boolean deleted = categoryService.delete(id);
        return ApiResponse.<Boolean>builder()
                .result(deleted)
                .build();
    }


}