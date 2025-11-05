//package com.java.shoes_service.controller;
//
//import com.java.common_dto.CloudinaryResponse;
//import com.java.product_service.dto.ApiResponse;
//import com.java.product_service.dto.PageResponse;
//import com.java.product_service.dto.request.ProductCreateRequest;
//import com.java.product_service.dto.request.ProductUpdateImageRequest;
//import com.java.product_service.dto.request.ProductUpdateRequest;
//import com.java.product_service.dto.response.ProductCreateResponse;
//import com.java.product_service.dto.response.ProductGetResponse;
//import com.java.product_service.service.ProductService;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class ProductController {
//    ProductService productService;
//
//
//    @GetMapping(value = "/{productId}")
//    public ApiResponse<ProductGetResponse> getDetailProductById(@PathVariable("productId") String productId)  {
//
//        ProductGetResponse response = productService.getProductById(productId);
//
//        return ApiResponse.<ProductGetResponse>builder()
//                .result(response)
//                .build();
//    }
//
//    @GetMapping(value = "/get-my-products")
//    public ApiResponse<PageResponse<ProductGetResponse>> getMyProduct(@RequestParam(defaultValue = "1") int page,
//                                                                      @RequestParam(defaultValue = "10") int size
//    ) {
//
//        PageResponse<ProductGetResponse> response = productService.getMyProducts(page, size);
//
//        return ApiResponse.<PageResponse<ProductGetResponse>>builder()
//                .result(response)
//                .build();
//    }
//    @GetMapping(value = "/get-products-by-user/{userId}")
//    public ApiResponse<PageResponse<ProductGetResponse>> getProductByUser(@RequestParam(defaultValue = "1") int page,
//                                                                      @RequestParam(defaultValue = "10") int size,
//                                                                          @PathVariable String userId
//    ) {
//
//        PageResponse<ProductGetResponse> response = productService.getProductsByUserId(page, size, userId);
//
//        return ApiResponse.<PageResponse<ProductGetResponse>>builder()
//                .result(response)
//                .build();
//    }
//    @GetMapping(value = "/get-products-by-category/{categoryId}")
//    public ApiResponse<PageResponse<ProductGetResponse>> getProductByCategory(@RequestParam(defaultValue = "1") int page,
//                                                                          @RequestParam(defaultValue = "10") int size,
//                                                                          @PathVariable String categoryId
//    ) {
//
//        PageResponse<ProductGetResponse> response = productService.getProductsByCategoryId(page, size, categoryId);
//
//        return ApiResponse.<PageResponse<ProductGetResponse>>builder()
//                .result(response)
//                .build();
//    }
//    @GetMapping(value = "/get-all-products")
//    public ApiResponse<PageResponse<ProductGetResponse>> getAllProduct(@RequestParam(defaultValue = "1") int page,
//                                                                       @RequestParam(defaultValue = "10") int size
//    ) {
//
//        PageResponse<ProductGetResponse> response = productService.getAllProduct(page, size);
//
//        return ApiResponse.<PageResponse<ProductGetResponse>>builder()
//                .result(response)
//                .build();
//    }
//
//    @GetMapping(value = "/search")
//    public ApiResponse<PageResponse<ProductGetResponse>> searchProducts(
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) Double minPrice,
//            @RequestParam(required = false) Double maxPrice,
//            @RequestParam(required = false) String categoryId
//    ) {
//        PageResponse<ProductGetResponse> response = productService.searchProducts(
//                page, size, name, minPrice, maxPrice, categoryId
//        );
//
//        return ApiResponse.<PageResponse<ProductGetResponse>>builder()
//                .result(response)
//                .build();
//    }
//
//    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ApiResponse<ProductCreateResponse> createProduct(
//            @RequestPart("request") ProductCreateRequest request,
//            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
//
//        return ApiResponse.<ProductCreateResponse>builder()
//                .result(productService.createProduct(request, files))
//                .build();
//    }
//
//    @DeleteMapping(value = "/delete")
//    public ApiResponse<Boolean> deleteProduct(
//            @RequestParam("productId") String productId) {
//
//        return ApiResponse.<Boolean>builder()
//                .result(productService.deleteProduct(productId))
//                .build();
//    }
//
//
//    @PatchMapping(value = "/update/content")
//    public ApiResponse<ProductGetResponse> updateProduct(@RequestBody ProductUpdateRequest request){
//        ProductGetResponse response = productService.updateProduct(request);
//
//        return ApiResponse.<ProductGetResponse>builder()
//                .result(response)
//                .build();
//    }
//
//    @PostMapping(value = "/update/image")
//    public ApiResponse<List<CloudinaryResponse>> updateImage(@RequestPart("request") ProductUpdateImageRequest request,
//                                                             @RequestPart(value = "files", required = false) List<MultipartFile> files)
//    {
//
//        List<CloudinaryResponse> response = productService.updateImageProduct(request, files);
//
//        return ApiResponse.<List<CloudinaryResponse>>builder()
//                .result(response)
//                .build();
//    }
//
//}