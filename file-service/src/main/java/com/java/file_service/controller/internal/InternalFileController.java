package com.java.file_service.controller.internal;


import com.java.CloudinaryResponse;
import com.java.FileDeleteAllRequest;
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

    @GetMapping(value = "/file/get-img")
    public ApiResponse<List<CloudinaryResponse>> getImageProduct(@RequestParam("id") String id) {

        List<CloudinaryResponse> response = fileService.getAllById(id);

        return ApiResponse.<List<CloudinaryResponse>>builder()
                .result(response)
                .build();
    }

    @PostMapping(value = "/file/product/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<CloudinaryResponse> uploadMediaProduct(@RequestPart("file") MultipartFile file ,
                                                       @RequestPart("productId") String productId) {
        return ApiResponse.<CloudinaryResponse>builder()
                .result(fileService.uploadFile(file, productId))
                .build();
    }

    @DeleteMapping(value = "/file/delete-all-img")
    public ApiResponse<Boolean> deleteAllImageProduct(@RequestBody FileDeleteAllRequest request) {

        Boolean response = fileService.deleteAllById(request.getId());
        return ApiResponse.<Boolean>builder()
                .result(response)
                .build();
    }


}