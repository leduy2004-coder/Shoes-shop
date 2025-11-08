package com.java.shoes_service.service;

import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.brand.BrandGetResponse;
import com.java.shoes_service.dto.brand.BrandRequest;
import com.java.shoes_service.entity.brand.BrandEntity;
import com.java.shoes_service.repository.BrandRepository;
import com.java.shoes_service.repository.httpClient.FileClient;
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

    public PageResponse<BrandGetResponse> searchBrands(
            int page, int size, String name, String sortBy, String sortOrder
    ) {
        Sort sort = resolveSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, size), sort);

        Page<BrandEntity> p;
        if (name == null || name.isBlank()) {
            p = brandRepository.findAll(pageable);  // ← lấy tất cả
        } else {
            // Regex “.*name.*” không phân biệt hoa/thường
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

    public BrandGetResponse create(BrandRequest request, MultipartFile file){
        BrandEntity brand = modelMapper.map(request, BrandEntity.class);
        brand = brandRepository.save(brand);

        var response = fileClient.uploadMediaBrand(file, brand.getId()).getResult();
        brand.setLogo(response.getUrl());
        brandRepository.save(brand);
        return mapToBrandGetResponse(brand);
    }

    private BrandGetResponse mapToBrandGetResponse(BrandEntity entity) {
        return modelMapper.map(entity, BrandGetResponse.class);
    }

    private Sort resolveSort(String sortBy, String sortOrder) {
        String field = (sortBy == null || sortBy.isBlank()) ? "createdDate" : sortBy.trim();
        Sort.Direction dir = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Whitelist các field hợp lệ để tránh sort field lạ
        return switch (field) {
            case "name", "createdDate", "modifiedDate" ->
                    Sort.by(dir, field);
            default -> Sort.by(Sort.Direction.DESC, "createdDate");
        };
    }
}

