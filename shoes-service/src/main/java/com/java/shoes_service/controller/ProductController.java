package com.java.shoes_service.controller;


import com.java.shoes_service.dto.ApiResponse;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.product.ProductGetResponse;
import com.java.shoes_service.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/products")
public class ProductController {
    ProductService productService;

    @GetMapping
    public ApiResponse<PageResponse<ProductGetResponse>> getAllProduct(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<ProductGetResponse> response = productService.getAllProduct(page, size);
        return ApiResponse.<PageResponse<ProductGetResponse>>builder()
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductGetResponse> getDetailProductById(@PathVariable("id") String productId) {
        ProductGetResponse response = productService.getProductById(productId);
        return ApiResponse.<ProductGetResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductGetResponse>> searchProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int page_size,
            @RequestParam(required = false, name = "brand_id") String brandId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false, name = "min_price") Double minPrice,
            @RequestParam(required = false, name = "max_price") Double maxPrice,
            @RequestParam(required = false, name = "category_id") String categoryId,
            @RequestParam(required = false, name = "sort_by") String sortBy,
            @RequestParam(required = false, name = "sort_order") String sortOrder
    ) {
        PageResponse<ProductGetResponse> response = productService.searchProducts(
                page, page_size, brandId, status, name, minPrice, maxPrice, categoryId, sortBy, sortOrder
        );
        return ApiResponse.<PageResponse<ProductGetResponse>>builder()
                .result(response)
                .build();
    }

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

}