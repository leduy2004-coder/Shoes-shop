package com.java.shoes_service.repository.httpClient;

import com.java.CloudinaryResponse;
import com.java.FileDeleteAllRequest;
import com.java.ImageType;
import com.java.shoes_service.config.security.AuthenticationRequestInterceptor;
import com.java.shoes_service.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "file-service",
        configuration = {AuthenticationRequestInterceptor.class})
public interface FileClient {
    @PostMapping(value = "/file/internal/product/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<CloudinaryResponse> uploadMediaProduct(@RequestPart("file") MultipartFile file,
                                                       @RequestPart("productId") String productId);

    @PostMapping(value = "/file/internal/banner/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<CloudinaryResponse> uploadMediaBanner(@RequestPart("file") MultipartFile file,
                                                       @RequestPart("bannerId") String bannerId);

    @PostMapping(value = "/file/internal/brand/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<CloudinaryResponse> uploadMediaBrand(@RequestPart("file") MultipartFile file,
                                                       @RequestPart("brandId") String brandId);

    @GetMapping(value = "/file/internal/get-img")
    ApiResponse<List<CloudinaryResponse>> getImage(@RequestParam("id") String id,
                                                   @RequestParam("type") ImageType type);

    @DeleteMapping(value = "/file/internal/delete-all-img")
    ApiResponse<Boolean> deleteAllImageProduct(@RequestBody FileDeleteAllRequest request,
                                               @RequestParam("type") ImageType type);

    @DeleteMapping(value = "/file/internal/delete-image-by-name")
    ApiResponse<Boolean> deleteByNameImage(@RequestBody String nameImage,
                                               @RequestParam("type") ImageType type);

}

