package com.java.file_service.controller.internal;


import com.java.CloudinaryResponse;
import com.java.FileDeleteAllRequest;
import com.java.ImageType;
import com.java.file_service.dto.ApiResponse;
import com.java.file_service.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalFileController {
    FileService fileService;

    @GetMapping(value = "/get-img")
    public ApiResponse<List<CloudinaryResponse>> getImageProduct(@RequestParam("id") String id,
                                                                 @RequestParam("type") ImageType type) {

        List<CloudinaryResponse> response = fileService.getAllById(id, type);

        return ApiResponse.<List<CloudinaryResponse>>builder()
                .result(response)
                .build();
    }

    @PostMapping(value = "/product/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<CloudinaryResponse> uploadMediaProduct(@RequestPart("file") MultipartFile file ,
                                                       @RequestPart("productId") String productId) {
        return ApiResponse.<CloudinaryResponse>builder()
                .result(fileService.uploadFile(file, ImageType.PRODUCT,productId))
                .build();
    }
    @PostMapping("/brand/upload")
    ApiResponse<CloudinaryResponse> uploadMediaPost(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("brandId") String brandId){
        return ApiResponse.<CloudinaryResponse>builder()
                .result(fileService.uploadFile(file, ImageType.BRAND, brandId))
                .build();
    }
    @PostMapping("/banner/upload")
    ApiResponse<CloudinaryResponse> uploadBanner(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("bannerId") String bannerId){
        return ApiResponse.<CloudinaryResponse>builder()
                .result(fileService.uploadFile(file, ImageType.BANNER, bannerId))
                .build();
    }
    @DeleteMapping(value = "/delete-all-img")
    public ApiResponse<Boolean> deleteAllImageProduct(@RequestBody FileDeleteAllRequest request,
                                                      @RequestParam("type") ImageType type) {

        Boolean response = fileService.deleteAllById(request.getId(), type);
        return ApiResponse.<Boolean>builder()
                .result(response)
                .build();
    }


}