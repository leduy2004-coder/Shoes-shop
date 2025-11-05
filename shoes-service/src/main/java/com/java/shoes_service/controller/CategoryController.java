//package com.java.shoes_service.controller;
//
//
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@RequestMapping("/categories")
//public class CategoryController {
//    CategoryService categoryService;
//
//
//    @GetMapping(value = "/get-all")
//    public ApiResponse<List<CategoriesResponse>> getAll() {
//
//        List<CategoriesResponse> response = categoryService.getAllCategory();
//
//        return ApiResponse.<List<CategoriesResponse>>builder()
//                .result(response)
//                .build();
//    }
//
//    @PostMapping(value = "/create")
//    public ApiResponse<CategoriesResponse> createCategory(
//            @RequestPart("request") CategoryCreateRequest categoryCreateRequest,
//            @RequestPart(value = "file", required = false) MultipartFile file) {
//        CategoriesResponse response = categoryService.createCategory(categoryCreateRequest,file);
//        return ApiResponse.<CategoriesResponse>builder()
//                .result(response)
//                .build();
//    }
//
//}