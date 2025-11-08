package com.java.shoes_service.service;

import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.product.ProductGetResponse;
import com.java.shoes_service.entity.product.ProductEntity;
import com.java.shoes_service.repository.ProductRepository;
import com.java.shoes_service.utility.ProductStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository productRepository;
    ModelMapper modelMapper;
    MongoTemplate mongoTemplate;

    public PageResponse<ProductGetResponse> getAllProduct(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdDate").descending());
        Page<ProductEntity> pageResult = productRepository.findAll(pageable);

        List<ProductGetResponse> data = pageResult.getContent()
                .stream().map(this::mapToProductGetResponse)
                .toList();

        return new PageResponse<>(
                page,
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                data
        );
    }

    public ProductGetResponse getProductById(String productId) {
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        return mapToProductGetResponse(entity);
    }

//    public PageResponse<ProductGetResponse> getProductRecent(int page, int size) {
//        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdDate").descending());
//        Page<ProductEntity> pageResult;
//
//        String userId = GetInfo.getLoggedInUserName();
//        if (userId == null) {
//            pageResult = productRepository.findAllByStatus(ProductStatus.ACTIVE, pageable);
//        } else {
//            List<String> categoryIds = recommendationClient.getRecent().getResult();
//            if (categoryIds == null || categoryIds.isEmpty()) {
//                pageResult = productRepository.findAllByStatus(ProductStatus.ACTIVE, pageable);
//            } else {
//                pageResult = productRepository.findAllByCategoryIdInAndStatus(
//                        categoryIds, ProductStatus.ACTIVE, pageable
//                );
//            }
//        }
//
//        List<ProductGetResponse> data = pageResult.getContent()
//                .stream().map(this::mapToProductGetResponse)
//                .toList();
//
//        return new PageResponse<>(
//                page,
//                pageResult.getSize(),
//                pageResult.getTotalElements(),
//                pageResult.getTotalPages(),
//                data
//        );
//    }

    public PageResponse<ProductGetResponse> searchProducts(
            int page, int size,
            String brandId,
            String statusStr,
            String name,
            Double minPrice,
            Double maxPrice,
            String categoryId,
            String sortBy,
            String sortOrder
    ) {
        // --- Build criteria ---
        List<Criteria> ands = new ArrayList<>();

        if (brandId != null && !brandId.isBlank()) {
            // DBRef Brand -> truy cập nested "brand.id"
            ands.add(Criteria.where("brand.id").is(brandId.trim()));
        }
        if (categoryId != null && !categoryId.isBlank()) {
            // DBRef Category -> "category.id"
            ands.add(Criteria.where("category.id").is(categoryId.trim()));
        }
        if (statusStr != null && !statusStr.isBlank()) {
            // chấp nhận "active"/"ACTIVE"
            try {
                ProductStatus st = ProductStatus.valueOf(statusStr.trim().toUpperCase());
                ands.add(Criteria.where("status").is(st));
            } catch (IllegalArgumentException ex) {
                // nếu Entity lưu status là String (không phải enum), vẫn hỗ trợ
                ands.add(Criteria.where("status").is(statusStr.trim()));
            }
        }
        if (name != null && !name.isBlank()) {
            // tìm theo tên, không phân biệt hoa/thường
            ands.add(Criteria.where("name").regex(name.trim(), "i"));
        }
        if (minPrice != null) {
            ands.add(Criteria.where("price").gte(minPrice));
        }
        if (maxPrice != null) {
            ands.add(Criteria.where("price").lte(maxPrice));
        }

        Query query = new Query();
        if (!ands.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(ands.toArray(Criteria[]::new)));
        }

        // --- Sort ---
        Sort sort = resolveSort(sortBy, sortOrder);
        query.with(sort);

        // --- Paging ---
        int pageIndex = Math.max(0, page - 1);
        int pageSize  = Math.max(1, size);
        query.skip((long) pageIndex * pageSize).limit(pageSize);

        // --- Execute ---
        List<ProductEntity> content = mongoTemplate.find(query, ProductEntity.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ProductEntity.class);

        // --- Map DTO ---
        List<ProductGetResponse> items = content.stream()
                .map(this::mapToProductGetResponse)
                .toList();

        int totalPages = (int) Math.ceil((double) total / pageSize);

        return new PageResponse<>(
                page,            // giữ nguyên style trả về page đã truyền vào (1-based)
                pageSize,
                total,
                totalPages,
                items
        );
    }


    private ProductGetResponse mapToProductGetResponse(ProductEntity entity) {
        return modelMapper.map(entity, ProductGetResponse.class);
    }

    private Sort resolveSort(String sortBy, String sortOrder) {
        String field = (sortBy == null || sortBy.isBlank()) ? "createdDate" : sortBy.trim();
        Sort.Direction dir = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Whitelist các field hợp lệ để tránh sort field lạ
        return switch (field) {
            case "price", "name", "createdDate", "modifiedDate", "discount", "stock" ->
                    Sort.by(dir, field);
            default -> Sort.by(Sort.Direction.DESC, "createdDate");
        };
    }


}
