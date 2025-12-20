package com.java.shoes_service.service;

import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.brand.BrandGetResponse;
import com.java.shoes_service.dto.brand.BrandRequest;
import com.java.shoes_service.entity.brand.BrandEntity;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.BrandRepository;
import com.java.shoes_service.repository.httpClient.FileClient;
import com.java.shoes_service.repository.product.ProductRepository;
import com.java.shoes_service.entity.product.ProductEntity;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandService {
    BrandRepository brandRepository;
    ModelMapper modelMapper;
    FileClient fileClient;
    ProductRepository productRepository;

    public PageResponse<BrandGetResponse> searchBrands(
            int page, int size, String name, String productId, String sortBy, String sortOrder
    ) {
        Sort sort = resolveSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), sort);

        Page<BrandEntity> p;
        
        // Nếu có productId, lấy brand của product đó
        String brandIdFilter = null;
        if (productId != null && !productId.isBlank()) {
            ProductEntity product = productRepository.findById(productId.trim())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
            if (product.getBrand() != null) {
                brandIdFilter = product.getBrand().getId();
            }
        }
        
        // Nếu có brandIdFilter, chỉ lấy brand đó (kết hợp với name filter nếu có)
        if (brandIdFilter != null) {
            BrandEntity brand = brandRepository.findById(brandIdFilter)
                    .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
            
            // Kiểm tra name filter nếu có
            if (name != null && !name.isBlank()) {
                String brandName = brand.getName();
                String pattern = ".*" + Pattern.quote(name.trim()) + ".*";
                if (!brandName.matches("(?i)" + pattern)) {
                    // Brand không khớp với name filter, trả về empty
                    return new PageResponse<>(page, size, 0, 0, List.of());
                }
            }
            
            // Trả về brand duy nhất
            List<BrandGetResponse> items = List.of(mapToBrandGetResponse(brand));
            return new PageResponse<>(page, 1, 1, 1, items);
        }
        
        // Không có productId, tìm theo name như cũ
        if (name == null || name.isBlank()) {
            p = brandRepository.findAll(pageable);  // ← lấy tất cả
        } else {
            // Regex ".*name.*" không phân biệt hoa/thường
            String pattern = ".*" + Pattern.quote(name.trim()) + ".*";
            p = brandRepository.findByNameRegexIgnoreCase(pattern, pageable);
        }

        List<BrandGetResponse> items = p.getContent().stream().map(this::mapToBrandGetResponse).toList();
        return new PageResponse<>(page, p.getSize(), p.getTotalElements(), p.getTotalPages(), items);
    }

    public BrandGetResponse getBrandById(String brandId) {
        BrandEntity entity = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Brand not found: " + brandId));
        return mapToBrandGetResponse(entity);
    }


    public BrandGetResponse createOrUpdate(BrandRequest request, MultipartFile file) {
        BrandEntity brand;
        String brandId = request.getId();
        
        // Nếu có id thì là update, không có thì là create
        if (brandId != null) {
            // Update
            brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
            
            // Cập nhật thông tin
            if (request.getName() != null && !request.getName().isBlank()) {
                brand.setName(request.getName());
            }
            
            // Xử lý logo: nếu có file mới thì upload, không có thì giữ logo cũ
            if (file != null && !file.isEmpty()) {
                var uploadResponse = fileClient.uploadMediaBrand(file, brandId).getResult();
                brand.setLogo(uploadResponse.getUrl());
            }

        } else {
            // Create
            brand = modelMapper.map(request, BrandEntity.class);
            brand = brandRepository.save(brand);
            // Nếu có file thì upload logo
            if (file != null && !file.isEmpty()) {
                var uploadResponse = fileClient.uploadMediaBrand(file, brand.getId()).getResult();
                brand.setLogo(uploadResponse.getUrl());
            }
        }
        brand = brandRepository.save(brand);
        return mapToBrandGetResponse(brand);
    }

    private BrandGetResponse mapToBrandGetResponse(BrandEntity entity) {
        BrandGetResponse response = modelMapper.map(entity, BrandGetResponse.class);
        // Lấy số lượng product của brand
        long countProduct = productRepository.countByBrand_Id(entity.getId());
        response.setCountProduct((int) countProduct);
        return response;
    }

    private Sort resolveSort(String sortBy, String sortOrder) {
        String field = (sortBy == null || sortBy.isBlank()) ? "createdDate" : sortBy.trim();
        Sort.Direction dir = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (field) {
            case "name", "createdDate", "modifiedDate" ->
                    Sort.by(dir, field);
            default -> Sort.by(Sort.Direction.DESC, "createdDate");
        };
    }

    public Boolean delete(String id) {
        long count = productRepository.countByBrand_Id(id);
        if (count > 0) {
            return false;
        }

        if (!brandRepository.existsById(id)) {
            return false;
        }

        brandRepository.deleteById(id);
        return true;
    }
}

