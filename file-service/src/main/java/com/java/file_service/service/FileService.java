package com.java.file_service.service;


import com.java.CloudinaryResponse;
import com.java.ImageType;
import com.java.ProductUploadRequest;
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

    public CloudinaryResponse uploadFile(MultipartFile file, ImageType imageType, String id, String primaryName) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String url = minioService.upload(file, fileName);
        boolean isPrimary = false;
        if (imageType.equals(ImageType.BRAND)) {
            brandRepository.save(BrandImageEntity.builder()
                    .brandId(id)
                    .name(fileName)
                    .url(url)
                    .build());
        } else if (imageType.equals(ImageType.BANNER)) {
            bannerRepository.save(BannerImageEntity.builder()
                    .bannerId(id)
                    .name(fileName)
                    .url(url)
                    .build());
        } else if (imageType.equals(ImageType.PRODUCT)) {
            if (primaryName != null){
                isPrimary = primaryName.equals(file.getOriginalFilename());
            }
            productRepository.save(ProductImageEntity.builder()
                    .productId(id)
                    .name(fileName)
                    .url(url)
                    .primary(isPrimary)
                    .build());
        }
        return CloudinaryResponse.builder()
                .isPrimary(isPrimary)
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
                                .isPrimary(product.getPrimary())
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
        } else if (imageType.equals(ImageType.PRODUCT)) {
            ProductImageEntity image = productRepository.findByName(name);
            minioService.delete(name);
            productRepository.deleteById(image.getId());
            return true;
        }
        return false;

    }

    public Boolean updatePrimaryImage(String productId, String primaryName) {
        if (productId == null || productId.isBlank() || primaryName == null || primaryName.isBlank()) {
            return false;
        }

        // Lấy tất cả ảnh của product
        List<ProductImageEntity> images = productRepository.findAllByProductId(productId);
        if (images.isEmpty()) {
            return false;
        }

        // Tìm ảnh có tên trùng với primaryName (cần extract tên gốc từ fileName)
        ProductImageEntity targetImage = null;
        for (ProductImageEntity img : images) {
            // fileName có format: UUID_originalName, cần check originalName
            String fileName = img.getName();
            int underscoreIndex = fileName.indexOf('_');
            if (underscoreIndex > 0 && underscoreIndex < fileName.length() - 1) {
                String originalName = fileName.substring(underscoreIndex + 1);
                if (primaryName.equals(originalName)) {
                    // Nếu có nhiều ảnh cùng originalName, lấy ảnh mới nhất (theo createdDate)
                    if (targetImage == null || img.getCreatedDate().isAfter(targetImage.getCreatedDate())) {
                        targetImage = img;
                    }
                }
            }
        }

        if (targetImage == null) {
            return false;
        }

        // Set tất cả ảnh về false
        images.forEach(img -> img.setPrimary(false));

        // Set ảnh target thành true
        targetImage.setPrimary(true);

        // Lưu lại
        productRepository.saveAll(images);

        return true;
    }

}