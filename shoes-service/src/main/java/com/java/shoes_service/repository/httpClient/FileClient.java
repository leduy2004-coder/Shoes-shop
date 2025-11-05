package com.java.shoes_service.repository.httpClient;

import com.java.CloudinaryResponse;
import com.java.FileDeleteAllRequest;
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
    @PostMapping(value = "/internal/file/product/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<CloudinaryResponse> uploadMediaProduct(@RequestPart("file") MultipartFile file,
                                                       @RequestPart("productId") String productId);


    @GetMapping(value = "/internal/file/get-img")
    ApiResponse<List<CloudinaryResponse>> getImage(@RequestParam("id") String id);

    @DeleteMapping(value = "/internal/file/delete-all-img")
    ApiResponse<Boolean> deleteAllImageProduct(@RequestBody FileDeleteAllRequest request);


}

