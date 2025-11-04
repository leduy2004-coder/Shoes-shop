package com.java.file_service.service;


import com.java.CloudinaryResponse;
import com.java.file_service.entity.ProductImageEntity;
import com.java.file_service.repository.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileService {
    MinioService minioService;
    ProductRepository productRepository;

    public CloudinaryResponse uploadFile(MultipartFile file, String id) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String url = minioService.upload(file, fileName);
        String imgId = productRepository.save(ProductImageEntity.builder()
                    .productId(id)
                    .name(fileName)
                    .url(url)
                    .build()).getId();

        return CloudinaryResponse.builder()
                .fileName(imgId)
                .url(url).build();
    }


    public List<CloudinaryResponse> getAllById(String id) {

        var list = productRepository.findAllByProductId(id);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.stream()
                    .map(product -> CloudinaryResponse.builder()
                            .fileName(product.getName())
                            .url(product.getUrl())
                            .build())
                    .collect(Collectors.toList());
        }
    }

    public Boolean deleteAllById(String id) {

            var list = productRepository.findAllByProductId(id);
            if (list.isEmpty()) {
                return false;
            }
            list.forEach(product -> {
                // Xóa ảnh khỏi Cloudinary
                if (product.getName() != null) {
                    minioService.delete(product.getName());
                }
                productRepository.deleteById(product.getId());
            });

            return true;
        }

}