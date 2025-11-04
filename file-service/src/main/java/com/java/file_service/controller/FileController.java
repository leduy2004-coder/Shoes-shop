package com.java.file_service.controller;

import com.java.CloudinaryResponse;
import com.java.file_service.dto.ApiResponse;
import com.java.file_service.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileController {
    FileService fileService;

    @PostMapping("/product/update")
    ApiResponse<CloudinaryResponse> uploadMedia(@RequestParam("file") MultipartFile file,
                                                @RequestParam("id") String id)  {
        return ApiResponse.<CloudinaryResponse>builder()
                .result(fileService.uploadFile(file,id))
                .build();
    }
}