package com.java.file_service.service;


import com.java.CloudinaryResponse;
import com.java.ImageType;
import com.java.file_service.entity.BannerImageEntity;
import com.java.file_service.entity.BrandImageEntity;
import com.java.file_service.entity.ProductImageEntity;
import com.java.file_service.repository.BannerRepository;
import com.java.file_service.repository.BrandRepository;
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
    BannerRepository bannerRepository;
    BrandRepository brandRepository;

    public CloudinaryResponse uploadFile(MultipartFile file, ImageType imageType, String id) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String url = minioService.upload(file, fileName);
        String imgId = "";
        if (imageType.equals(ImageType.BRAND)) {
            imgId = brandRepository.save(BrandImageEntity.builder()
                    .brandId(id)
                    .name(fileName)
                    .url(url)
                    .build()).getId();
        } else if (imageType.equals(ImageType.BANNER)) {
            imgId = bannerRepository.save(BannerImageEntity.builder()
                    .bannerId(id)
                    .name(fileName)
                    .url(url)
                    .build()).getId();
        } else if (imageType.equals(ImageType.PRODUCT)) {
            imgId = productRepository.save(ProductImageEntity.builder()
                    .productId(id)
                    .name(fileName)
                    .url(url)
                    .build()).getId();
        }
        return CloudinaryResponse.builder()
                .fileName(fileName)
                .url(url).build();
    }


    public List<CloudinaryResponse> getAllById(String id, ImageType imageType) {

        if (imageType.equals(ImageType.BANNER)) {
            var list = bannerRepository.findAllByBannerId(id);

            return list.stream()
                    .map(product -> CloudinaryResponse.builder()
                            .fileName(product.getName())
                            .url(product.getUrl())
                            .build())
                    .collect(Collectors.toList());
        }
        if (imageType.equals(ImageType.BRAND)) {
            var list = brandRepository.findAllByBrandId(id);
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
        if (imageType.equals(ImageType.PRODUCT)) {
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

        return null;

    }

    public Boolean deleteAllById(String id, ImageType imageType) {

        if (imageType.equals(ImageType.BRAND)) {
            var list = brandRepository.findAllByBrandId(id);

            if (list.isEmpty()) {
                return false;
            }
            list.forEach(brand -> {
                if (brand.getName() != null) {
                    minioService.delete(brand.getName());
                }
                brandRepository.deleteById(brand.getId());
            });
            return true;
        }
        if (imageType.equals(ImageType.PRODUCT)) {
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
        if (imageType.equals(ImageType.BANNER)) {
            var list = bannerRepository.findAllByBannerId(id);
            if (list.isEmpty()) {
                return false;
            }
            list.forEach(banner -> {
                // Xóa ảnh khỏi Cloudinary
                if (banner.getName() != null) {
                    minioService.delete(banner.getName());
                }
                bannerRepository.deleteById(banner.getId());
            });

            return true;
        }
        return false;

    }

    public Boolean deleteByNameImage(String name, ImageType imageType) {
        if (imageType.equals(ImageType.BANNER)) {
            BannerImageEntity image = bannerRepository.findByName(name);
            minioService.delete(name);
            bannerRepository.deleteById(image.getId());
            return true;
        }
        return false;

    }

}