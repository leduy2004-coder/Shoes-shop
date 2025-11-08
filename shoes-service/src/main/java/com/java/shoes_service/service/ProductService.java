package com.java.shoes_service.service;

import com.java.CloudinaryResponse;
import com.java.ImageType;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.product.product.ProductCreateRequest;
import com.java.shoes_service.dto.product.product.ProductCreateResponse;
import com.java.shoes_service.dto.product.product.ProductGetDetailResponse;
import com.java.shoes_service.dto.product.product.ProductGetResponse;
import com.java.shoes_service.dto.product.variant.VariantCreateRequest;
import com.java.shoes_service.entity.brand.BrandEntity;
import com.java.shoes_service.entity.product.CategoryEntity;
import com.java.shoes_service.entity.product.ProductEntity;
import com.java.shoes_service.entity.product.SizeLabel;
import com.java.shoes_service.entity.product.VariantEntity;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.BrandRepository;
import com.java.shoes_service.repository.CategoryRepository;
import com.java.shoes_service.repository.ProductRepository;
import com.java.shoes_service.repository.VariantRepository;
import com.java.shoes_service.repository.httpClient.FileClient;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository productRepository;
    ModelMapper modelMapper;
    FileClient fileClient;
    MongoTemplate mongoTemplate;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    VariantRepository variantRepository;

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

    public ProductGetDetailResponse getProductById(String productId) {
        ProductEntity entity = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        ProductGetResponse productGetResponse = modelMapper.map(entity, ProductGetResponse.class);

        List<VariantEntity> list = variantRepository.findByProductId(productId);
        List<CloudinaryResponse> listImage = fileClient.getImage(productId, ImageType.PRODUCT).getResult();
        return ProductGetDetailResponse.builder().product(productGetResponse).variants(list).listImg(listImage).build();
    }

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
        // 1) Build criteria
        List<Criteria> ands = new ArrayList<>();

        if (brandId != null && !brandId.isBlank()) {
            // DBRef -> brand.id
            ands.add(Criteria.where("brand.id").is(brandId.trim()));
        }
        if (categoryId != null && !categoryId.isBlank()) {
            // DBRef -> category.id
            ands.add(Criteria.where("category.id").is(categoryId.trim()));
        }
        if (statusStr != null && !statusStr.isBlank()) {
            // Hỗ trợ enum lẫn string trong DB
            try {
                var st = com.java.shoes_service.utility.ProductStatus.valueOf(statusStr.trim().toUpperCase());
                ands.add(Criteria.where("status").is(st));
            } catch (IllegalArgumentException ignore) {
                ands.add(Criteria.where("status").is(statusStr.trim()));
            }
        }
        if (name != null && !name.isBlank()) {
            ands.add(Criteria.where("name").regex(name.trim(), "i")); // contains, case-insensitive
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

        // 2) Sort
        Sort sort = resolveSort(sortBy, sortOrder);
        query.with(sort);

        // 3) Paging (controller 1-based)
        int pageIndex = Math.max(0, page - 1);
        int pageSize  = Math.max(1, size);
        query.skip((long) pageIndex * pageSize).limit(pageSize);

        // 4) Execute
        List<com.java.shoes_service.entity.product.ProductEntity> content =
                mongoTemplate.find(query, com.java.shoes_service.entity.product.ProductEntity.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1),
                com.java.shoes_service.entity.product.ProductEntity.class);

        // 5) Map DTO
        List<ProductGetResponse> items = content.stream()
                .map(this::mapToProductGetResponse)
                .toList();

        int totalPages = (int) Math.ceil((double) total / pageSize);

        return new PageResponse<>(
                page,            // giữ 1-based như đầu vào
                pageSize,
                total,
                totalPages,
                items
        );
    }


    public ProductCreateResponse createProduct(ProductCreateRequest request, List<MultipartFile> files) {
        try {
            ProductEntity product = modelMapper.map(request, ProductEntity.class);

            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
            BrandEntity brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
            product.setCategory(category);
            product.setBrand(brand);

            if (product.getStatus() == null) {
                product.setStatus(ProductStatus.ACTIVE);
            }
            product.setCountSell(0);

            product.setAverageRating(0);

            int totalStock = (request.getVariants() == null) ? 0
                    : request.getVariants().stream().mapToInt(VariantCreateRequest::getStock).sum();
            product.setTotalStock(totalStock);

            ProductEntity entity = productRepository.save(product);

            String productId = entity.getId();

            //save variants
            request.getVariants().forEach(vReq -> {
                VariantEntity ve = modelMapper.map(vReq, VariantEntity.class);
                ve.setProductId(productId);
                ve.setStatus(ProductStatus.ACTIVE);
                ve.setCountSell(vReq.getCountSell());

                //  gán size
                SizeLabel size = SizeLabel.builder()
                        .label(String.valueOf(vReq.getSize()))
                        .build();
                ve.setSize(size);

                variantRepository.save(ve);
            });

            //upload images
            List<CloudinaryResponse> imgUrls = (files == null ? List.<MultipartFile>of() : files).stream()
                    .map(file -> fileClient.uploadMediaProduct(file, productId).getResult())
                    .toList();

            ProductCreateResponse response = modelMapper.map(entity, ProductCreateResponse.class);
            response.setImgUrls(imgUrls);


            return response;

        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            // log nếu cần
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private ProductGetResponse mapToProductGetResponse(ProductEntity entity) {
        ProductGetResponse response = modelMapper.map(entity, ProductGetResponse.class);
        response.setImageUrl(fileClient.getImage(entity.getId(), ImageType.PRODUCT).getResult().get(0));

        return response;
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
