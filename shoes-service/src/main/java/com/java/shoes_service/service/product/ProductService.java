package com.java.shoes_service.service.product;

import com.java.CloudinaryResponse;
import com.java.ImageType;
import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.product.product.*;
import com.java.shoes_service.entity.brand.BrandEntity;
import com.java.shoes_service.entity.product.CategoryEntity;
import com.java.shoes_service.entity.product.ProductEntity;
import com.java.shoes_service.entity.product.VariantEntity;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.*;
import com.java.shoes_service.repository.httpClient.FileClient;
import com.java.shoes_service.repository.product.CategoryRepository;
import com.java.shoes_service.repository.product.HistoryProductRepository;
import com.java.shoes_service.repository.product.ProductRepository;
import com.java.shoes_service.repository.product.VariantRepository;
import com.java.shoes_service.repository.promotion.ReviewRepository;
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
    HistoryProductRepository historyProductRepository;
    ReviewRepository reviewRepository;
    VariantService variantService;

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
                var st = ProductStatus.valueOf(statusStr.trim().toUpperCase());
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
        List<ProductEntity> content =
                mongoTemplate.find(query, ProductEntity.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1),
                ProductEntity.class);

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

            ProductEntity entity = productRepository.save(product);

            String productId = entity.getId();

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

    public ProductGetDetailResponse updateContentProduct(ProductUpdateRequest request) {

        ProductEntity entity = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        // Patch từng field nếu không null (hoặc dùng Optional.ofNullable)
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
            entity.setCategory(category);
        }
        if (request.getBrandId() != null && !request.getBrandId().isBlank()) {
            BrandEntity brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
            entity.setBrand(brand);
        }
        if (request.getName() != null)        entity.setName(request.getName());
        if (request.getSlug() != null)        entity.setSlug(request.getSlug());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getPrice() > 0)           entity.setPrice(request.getPrice());
        // discount có thể = 0, nên patch theo null-check: dùng Double để nhận null nếu muốn
        entity.setDiscount(request.getDiscount());

        // Lưu lại
        entity = productRepository.save(entity);

        // Build detail response (product + variants + list images)
        ProductGetResponse productDto = modelMapper.map(entity, ProductGetResponse.class);
        List<VariantEntity> variants = variantRepository.findByProductId(entity.getId());
        List<CloudinaryResponse> images = fileClient.getImage(entity.getId(), ImageType.PRODUCT).getResult();

        return ProductGetDetailResponse.builder()
                .product(productDto)
                .variants(variants)
                .listImg(images)
                .build();
    }

    public List<CloudinaryResponse> updateImageProduct(ProductUpdateImageRequest request,
                                                       List<MultipartFile> files) {

        // validate product
        if (!productRepository.existsById(request.getProductId())) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        // 1) Xoá ảnh cũ theo danh sách name
        if (request.getNames() != null && !request.getNames().isEmpty()) {
            for (String name : request.getNames()) {
                if (name != null && !name.isBlank()) {
                    try {
                        fileClient.deleteByNameImage(name, ImageType.PRODUCT);
                    } catch (Exception ex) {
                        log.warn("Cannot delete image name={} of productId={}: {}", name, request.getProductId(), ex.getMessage());
                    }
                }
            }
        }

        // 2) Upload ảnh mới (nếu có)
        if (files != null && !files.isEmpty()) {
            for (MultipartFile f : files) {
                if (f != null && !f.isEmpty()) {
                    fileClient.uploadMediaProduct(f, request.getProductId());
                }
            }
        }
        return fileClient.getImage(request.getProductId(), ImageType.PRODUCT).getResult();
    }
    // Top 5 theo averageRating desc, tie-break theo countSell desc, rồi createdDate desc
    public List<ProductGetResponse> getTopRatedTop5() {
        Pageable top5 = PageRequest.of(0, 5,
                Sort.by(Sort.Order.desc("averageRating"),
                        Sort.Order.desc("countSell"),
                        Sort.Order.desc("createdDate")));
        Page<ProductEntity> p = productRepository.findAll(top5);
        return p.getContent().stream().map(this::mapToProductGetResponse).toList();
    }

    // Xoá sạch: ảnh → history theo variant → variants → product
    public Boolean deleteProduct(String productId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        // Lấy variants để xoá history + cart items theo variant
        List<VariantEntity> variants = variantRepository.findByProductId(productId);
        if (!variants.isEmpty()) {
            List<String> vIds = variants.stream().map(VariantEntity::getId).toList();

            // Xoá cart items theo variantIds (phòng xa)

            vIds.forEach(variantService::deleteVariant);

            // Xoá history
            try {
                historyProductRepository.deleteByVariantIdIn(vIds);
            } catch (Exception e) {
                log.warn("deleteByVariantIdIn not available, fallback find+delete: {}", e.getMessage());
                historyProductRepository.findAllByVariantIdIn(vIds)
                        .forEach(h -> historyProductRepository.deleteById(h.getId()));
            }
        }

        // Xoá ảnh
        try {
            var imgs = fileClient.getImage(productId, ImageType.PRODUCT).getResult();
            if (imgs != null) {
                for (var img : imgs) {
                    if (img != null && img.getFileName() != null) {
                        try {
                            fileClient.deleteByNameImage(img.getFileName(), ImageType.PRODUCT);
                        } catch (Exception ex) {
                            log.warn("Delete image {} failed: {}", img.getFileName(), ex.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Get/Delete images error for product {}: {}", productId, e.getMessage());
        }



        // 3) Xoá reviews
        reviewRepository.deleteByProductId(productId);

        // 5) Xoá product
        productRepository.delete(product);
        return true;
    }


    private ProductGetResponse mapToProductGetResponse(ProductEntity entity) {
        ProductGetResponse response = modelMapper.map(entity, ProductGetResponse.class);
        response.setImageUrl(fileClient.getImage(entity.getId(), ImageType.PRODUCT).getResult().get(0));

        return response;
    }

    private Sort resolveSort(String sortBy, String sortOrder) {
        String field = (sortBy == null || sortBy.isBlank()) ? "createdDate" : sortBy.trim();
        Sort.Direction dir = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (field) {
            case "name",
                 "price",
                 "discount",
                 "totalStock",
                 "averageRating",
                 "countSell",
                 "status",
                 "createdDate",
                 "modifiedDate" -> Sort.by(dir, field);
            default -> Sort.by(Sort.Direction.DESC, "createdDate");
        };
    }


}
