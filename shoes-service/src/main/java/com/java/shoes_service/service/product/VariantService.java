package com.java.shoes_service.service.product;

import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.product.product.ProductGetResponse;
import com.java.shoes_service.dto.product.variant.*;
import com.java.shoes_service.entity.product.HistoryProductEntity;
import com.java.shoes_service.entity.product.ProductEntity;
import com.java.shoes_service.entity.product.VariantEntity;
import com.java.shoes_service.entity.product.VariantSizeEntity;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.CartItemRepository;
import com.java.shoes_service.repository.product.HistoryProductRepository;
import com.java.shoes_service.repository.product.ProductRepository;
import com.java.shoes_service.repository.product.VariantRepository;
import com.java.shoes_service.repository.product.VariantSizeRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VariantService {
    VariantRepository variantRepository;
    VariantSizeRepository variantSizeRepository;
    ProductRepository productRepository;
    HistoryProductRepository historyProductRepository;
    ModelMapper modelMapper;
    CartItemRepository cartItemRepository;

    public List<VariantResponse> upsertVariant(VariantCreateRequest request) {
        try {
            if (request == null || request.getProductId() == null || request.getProductId().isBlank()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            if (request.getVariants() == null || request.getVariants().isEmpty()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            ProductEntity product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

            List<VariantResponse> results = new ArrayList<>();

            // Xử lý từng VariantRequest
            for (VariantRequest vReq : request.getVariants()) {
                VariantEntity variant;
                
                // Nếu VariantRequest có id (variantId) → UPDATE mode cho variant này
                if (vReq.getId() != null && !vReq.getId().isBlank()) {
                    // UPDATE mode: Update variant đã tồn tại
                    variant = variantRepository.findById(vReq.getId())
                            .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

                    // Validate: variant phải thuộc product
                    if (!variant.getProductId().equals(request.getProductId())) {
                        throw new AppException(ErrorCode.INVALID_REQUEST); // variant không thuộc product
                    }

                    String color = vReq.getColor();
                    // Update color nếu có thay đổi
                    if (color != null && !color.isBlank() && !color.equals(variant.getColor())) {
                        // Kiểm tra xem color mới đã tồn tại cho product này chưa (trừ variant hiện tại)
                        VariantEntity finalVariant = variant;
                        boolean colorExists = variantRepository.findByProductId(request.getProductId()).stream()
                                .anyMatch(v -> !v.getId().equals(finalVariant.getId())
                                        && v.getColor().equalsIgnoreCase(color));
                        if (colorExists) {
                            throw new AppException(ErrorCode.VARIANT_DUPLICATED);
                        }
                        variant.setColor(color);
                        variant = variantRepository.save(variant);
                    }
                } else {
                    // CREATE mode: Tạo variant mới
                    String color = vReq.getColor();
                    if (color == null || color.isBlank()) {
                        throw new AppException(ErrorCode.INVALID_REQUEST);
                    }

                    if (vReq.getSizes() == null || vReq.getSizes().isEmpty()) {
                        throw new AppException(ErrorCode.INVALID_REQUEST);
                    }

                    // Tìm hoặc tạo VariantEntity (productId + color)
                    variant = variantRepository
                            .findByProductIdAndColorIgnoreCase(product.getId(), color)
                            .orElse(null);

                    if (variant == null) {
                        // Tạo VariantEntity mới nếu chưa có
                        variant = VariantEntity.builder()
                                .productId(product.getId())
                                .color(color)
                                .status(ProductStatus.ACTIVE)
                                .build();
                        variant = variantRepository.save(variant);
                    }
                }

                // Xử lý từng VariantSizeRequest (mỗi size có thể có id đi kèm)
                if (vReq.getSizes() != null && !vReq.getSizes().isEmpty()) {
                    for (VariantSizeRequest sizeReq : vReq.getSizes()) {
                        if (sizeReq.getSize() == null) {
                            throw new AppException(ErrorCode.INVALID_REQUEST);
                        }

                        String sizeStr = String.valueOf(sizeReq.getSize());

                        // Nếu có id → UPDATE mode cho variantSize này
                        if (sizeReq.getId() != null && !sizeReq.getId().isBlank()) {
                            VariantSizeEntity variantSize = variantSizeRepository.findById(sizeReq.getId())
                                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

                            // Validate: variantSize phải thuộc variant này
                            if (!variantSize.getVariantId().equals(variant.getId())) {
                                throw new AppException(ErrorCode.INVALID_REQUEST);
                            }

                            // Update size nếu có thay đổi
                            if (!sizeStr.equals(variantSize.getSize())) {
                                // Kiểm tra xem size mới đã tồn tại chưa (trừ variantSize hiện tại)
                                boolean sizeExists = variantSizeRepository.existsByVariantIdAndSize(
                                        variant.getId(), sizeStr);
                                if (sizeExists) {
                                    throw new AppException(ErrorCode.VARIANT_DUPLICATED);
                                }
                                variantSize.setSize(sizeStr);
                                variantSizeRepository.save(variantSize);
                            }

                            // Map sang response
                            VariantResponse response = VariantResponse.builder()
                                    .id(variantSize.getId())
                                    .productId(variant.getProductId())
                                    .color(variant.getColor())
                                    .status(variant.getStatus())
                                    .size(variantSize.getSize())
                                    .stock(variantSize.getStock())
                                    .countSell(variantSize.getCountSell())
                                    .build();
                            results.add(response);
                        } else {
                            // Không có id → CREATE mode: Tạo VariantSizeEntity mới
                            // Kiểm tra xem size đã tồn tại chưa
                            if (variantSizeRepository.existsByVariantIdAndSize(variant.getId(), sizeStr)) {
                                throw new AppException(ErrorCode.VARIANT_DUPLICATED);
                            }

                            VariantSizeEntity newVariantSize = VariantSizeEntity.builder()
                                    .variantId(variant.getId())
                                    .size(sizeStr)
                                    .stock(0)
                                    .countSell(0)
                                    .build();
                            newVariantSize = variantSizeRepository.save(newVariantSize);

                            VariantResponse response = VariantResponse.builder()
                                    .id(newVariantSize.getId())
                                    .productId(variant.getProductId())
                                    .color(variant.getColor())
                                    .status(variant.getStatus())
                                    .size(newVariantSize.getSize())
                                    .stock(newVariantSize.getStock())
                                    .countSell(newVariantSize.getCountSell())
                                    .build();
                            results.add(response);
                        }
                    }
                }
            }

            return results;

        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            log.error("Error upserting variant", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public List<VariantResponse> importStocks(VariantStockImportListRequest req) {
        // 1) Validate đầu vào
        if (req == null || req.getProductId() == null || req.getProductId().isBlank())
            throw new AppException(ErrorCode.INVALID_REQUEST);
        if (!productRepository.existsById(req.getProductId()))
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        if (req.getItems() == null || req.getItems().isEmpty())
            throw new AppException(ErrorCode.INVALID_REQUEST);

        // Lấy tất cả VariantSizeEntity cần cập nhật (variantSizeId trong request)
        List<String> variantSizeIds = req.getItems().stream()
                .map(VariantStockImportItem::getVariantSizeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (variantSizeIds.isEmpty()) throw new AppException(ErrorCode.INVALID_REQUEST);

        Map<String, VariantSizeEntity> variantSizeMap = variantSizeRepository.findAllById(variantSizeIds).stream()
                .collect(Collectors.toMap(VariantSizeEntity::getId, vs -> vs));

        // Lấy tất cả VariantEntity liên quan để validate
        List<String> variantIds = variantSizeMap.values().stream()
                .map(VariantSizeEntity::getVariantId)
                .distinct()
                .toList();
        Map<String, VariantEntity> variantMap = variantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(VariantEntity::getId, v -> v));

        List<VariantSizeEntity> toUpdate = new ArrayList<>();
        List<HistoryProductEntity> histories = new ArrayList<>();

        // 2) Cập nhật từng item (cộng/trừ tồn), ghi history
        for (VariantStockImportItem it : req.getItems()) {
            if (it.getVariantSizeId() == null || it.getVariantSizeId().isBlank()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            
            VariantSizeEntity variantSize = variantSizeMap.get(it.getVariantSizeId());
            if (variantSize == null) throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
            
            VariantEntity variant = variantMap.get(variantSize.getVariantId());
            if (variant == null) throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
            
            if (!req.getProductId().equals(variant.getProductId()))
                throw new AppException(ErrorCode.INVALID_REQUEST); // variant không thuộc product

            if (it.getCount() == 0) continue; // bỏ qua delta = 0

            long newStock = (long) variantSize.getStock() + it.getCount();
            if (newStock < 0) throw new AppException(ErrorCode.INVALID_REQUEST); // không cho âm

            variantSize.setStock((int) newStock);
            toUpdate.add(variantSize);

            HistoryProductEntity h = new HistoryProductEntity();
            h.setVariantSizeId(variantSize.getId()); // Lưu variantSizeId vào history
            h.setCount(it.getCount()); // dương: nhập, âm: xuất/giảm
            histories.add(h);
        }

        if (!toUpdate.isEmpty()) variantSizeRepository.saveAll(toUpdate);
        if (!histories.isEmpty()) historyProductRepository.saveAll(histories);

        // 3) Recalc totalStock = tổng stock của tất cả VariantSizeEntity thuộc product
        List<VariantEntity> allVariants = variantRepository.findByProductId(req.getProductId());
        List<String> allVariantIds = allVariants.stream()
                .map(VariantEntity::getId)
                .toList();
        List<VariantSizeEntity> allVariantSizes = variantSizeRepository.findByVariantIdIn(allVariantIds);
        int totalStock = allVariantSizes.stream().mapToInt(VariantSizeEntity::getStock).sum();
        productRepository.findById(req.getProductId()).ifPresent(p -> {
            p.setTotalStock(totalStock);
            productRepository.save(p);
        });

        // 4) Trả về các variant đã cập nhật
        return toUpdate.stream()
                .map(vs -> {
                    VariantEntity v = variantMap.get(vs.getVariantId());
                    return VariantResponse.builder()
                            .id(vs.getId())
                            .productId(v.getProductId())
                            .color(v.getColor())
                            .status(v.getStatus())
                            .size(vs.getSize())
                            .stock(vs.getStock())
                            .countSell(vs.getCountSell())
                            .build();
                })
                .toList();
    }

    public PageResponse<VariantHistoryResponse> getHistory(String variantSizeId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdDate")
        );

        // variantSizeId là ID của VariantSizeEntity
        Page<HistoryProductEntity> p = (variantSizeId == null || variantSizeId.isBlank())
                ? historyProductRepository.findAll(pageable)
                : historyProductRepository.findByVariantSizeId(variantSizeId, pageable);

        // --- Batch load VariantSizeEntity ---
        List<String> variantSizeIds = p.getContent().stream()
                .map(HistoryProductEntity::getVariantSizeId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, VariantSizeEntity> variantSizeMap = variantSizeIds.isEmpty()
                ? Map.of()
                : variantSizeRepository.findAllById(variantSizeIds).stream()
                .collect(Collectors.toMap(VariantSizeEntity::getId, vs -> vs));

        // --- Batch load VariantEntity ---
        List<String> variantIds = variantSizeMap.values().stream()
                .map(VariantSizeEntity::getVariantId)
                .distinct()
                .toList();

        Map<String, VariantEntity> variantMap = variantIds.isEmpty()
                ? Map.of()
                : variantRepository.findAllById(variantIds).stream()
                .collect(Collectors.toMap(VariantEntity::getId, v -> v));

        // --- Batch load products ---
        List<String> productIds = variantMap.values().stream()
                .map(VariantEntity::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, ProductEntity> productMap = productIds.isEmpty()
                ? Map.of()
                : productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, pdt -> pdt));

        // --- Map to DTOs ---
        List<VariantHistoryResponse> items = p.getContent().stream()
                .map(h -> {
                    VariantSizeEntity variantSize = variantSizeMap.get(h.getVariantSizeId());
                    VariantEntity variant = (variantSize != null) ? variantMap.get(variantSize.getVariantId()) : null;
                    ProductEntity prod = (variant != null) ? productMap.get(variant.getProductId()) : null;

                    VariantHistoryResponse.VariantHistoryResponseBuilder b = VariantHistoryResponse.builder()
                            .id(h.getId())
                            .count(h.getCount());

                    if (variantSize != null && variant != null) {
                        VariantResponse variantResponse = VariantResponse.builder()
                                .id(variantSize.getId())
                                .productId(variant.getProductId())
                                .color(variant.getColor())
                                .status(variant.getStatus())
                                .size(variantSize.getSize())
                                .stock(variantSize.getStock())
                                .countSell(variantSize.getCountSell())
                                .build();
                        
                        b.variant(variantResponse)
                                .color(variant.getColor())
                                .size(variantSize.getSize());
                    }

                    if (prod != null) {
                        b.product(modelMapper.map(prod, ProductGetResponse.class));
                    }

                    return b.build();
                })
                .toList();

        return new PageResponse<>(
                page,                 // giữ 1-based
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                items
        );
    }

    public VariantResponse updateVariant(VariantUpdateRequest request) {
        // request.getId() là ID của VariantSizeEntity
        VariantSizeEntity variantSize = variantSizeRepository.findById(request.getId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
        
        VariantEntity variant = variantRepository.findById(variantSize.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
        
        // Cập nhật color của VariantEntity nếu có thay đổi
        if (request.getColor() != null && !request.getColor().equals(variant.getColor())) {
            variant.setColor(request.getColor());
            variantRepository.save(variant);
        }
        
        // Cập nhật size của VariantSizeEntity nếu có thay đổi
        if (request.getSize() != null && !request.getSize().equals(variantSize.getSize())) {
            // Kiểm tra xem size mới đã tồn tại cho variant này chưa
            if (variantSizeRepository.existsByVariantIdAndSize(variantSize.getVariantId(), request.getSize())) {
                throw new AppException(ErrorCode.VARIANT_DUPLICATED);
            }
            variantSize.setSize(request.getSize());
            variantSizeRepository.save(variantSize);
        }

        return VariantResponse.builder()
                .id(variantSize.getId())
                .productId(variant.getProductId())
                .color(variant.getColor())
                .status(variant.getStatus())
                .size(variantSize.getSize())
                .stock(variantSize.getStock())
                .countSell(variantSize.getCountSell())
                .build();
    }

    @Transactional
    public boolean deleteVariantSize(String variantSizeId) {
        // Xóa VariantSizeEntity cụ thể theo variantSizeId
        VariantSizeEntity variantSize = variantSizeRepository.findById(variantSizeId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        // Kiểm tra xem variantSize còn trong giỏ hàng không
        if (cartItemRepository.existsByVariant_Id(variantSizeId)) {
            throw new AppException(ErrorCode.VARIANT_IN_CART);
        }

        // Lấy thông tin variant và product để recalc sau khi xóa
        VariantEntity variant = variantRepository.findById(variantSize.getVariantId()).orElse(null);
        String productId = null;
        if (variant != null) {
            productId = variant.getProductId();
        }

        // 1) Xóa history của variantSize
        try {
            historyProductRepository.deleteByVariantSizeId(variantSizeId);
        } catch (Exception ignore) {
            historyProductRepository.findByVariantSizeId(variantSizeId, PageRequest.of(0, Integer.MAX_VALUE))
                    .forEach(h -> historyProductRepository.deleteById(h.getId()));
        }

        // 2) Xóa variantSize
        variantSizeRepository.deleteById(variantSizeId);

        // 3) Nếu không còn variantSize nào cho variant này, xóa variant
        if (variant != null) {
            List<VariantSizeEntity> remainingSizes = variantSizeRepository.findByVariantId(variant.getId());
            if (remainingSizes.isEmpty()) {
                // Không còn size nào, xóa variant
                variantRepository.deleteById(variant.getId());
            }
        }

        // 4) Recalc product.totalStock
        if (productId != null) {
            List<VariantEntity> allVariants = variantRepository.findByProductId(productId);
            List<String> allVariantIds = allVariants.stream()
                    .map(VariantEntity::getId)
                    .toList();
            List<VariantSizeEntity> allVariantSizes = variantSizeRepository.findByVariantIdIn(allVariantIds);
            int totalStock = allVariantSizes.stream().mapToInt(VariantSizeEntity::getStock).sum();
            productRepository.findById(productId).ifPresent(p -> {
                p.setTotalStock(totalStock);
                productRepository.save(p);
            });
        }

        return true;
    }

    @Transactional
    public boolean deleteVariant(String variantId) {
        // variantId ở đây có thể là VariantEntity ID hoặc VariantSizeEntity ID
        // Kiểm tra xem là VariantSizeEntity trước
        VariantSizeEntity variantSize = variantSizeRepository.findById(variantId).orElse(null);
        
        if (variantSize != null) {
            // Xóa VariantSizeEntity cụ thể
            String productId = null;
            VariantEntity variant = variantRepository.findById(variantSize.getVariantId()).orElse(null);
            if (variant != null) {
                productId = variant.getProductId();
            }

            // Nếu vẫn còn trong giỏ hàng -> không cho xóa
            if (cartItemRepository.existsByVariant_Id(variantId)) {
                throw new AppException(ErrorCode.VARIANT_IN_CART);
            }

            // 1) Xóa history của variantSize
            try {
                historyProductRepository.deleteByVariantSizeId(variantId);
            } catch (Exception ignore) {
                historyProductRepository.findByVariantSizeId(variantId, PageRequest.of(0, Integer.MAX_VALUE))
                        .forEach(h -> historyProductRepository.deleteById(h.getId()));
            }

            // 2) Xóa variantSize
            variantSizeRepository.deleteById(variantId);

            // 3) Nếu không còn variantSize nào cho variant này, có thể xóa variant
            if (variant != null) {
                List<VariantSizeEntity> remainingSizes = variantSizeRepository.findByVariantId(variant.getId());
                if (remainingSizes.isEmpty()) {
                    // Không còn size nào, xóa variant
                    variantRepository.deleteById(variant.getId());
                }

                // Recalc product.totalStock
                if (productId != null) {
                    List<VariantEntity> allVariants = variantRepository.findByProductId(productId);
                    List<String> allVariantIds = allVariants.stream()
                            .map(VariantEntity::getId)
                            .toList();
                    List<VariantSizeEntity> allVariantSizes = variantSizeRepository.findByVariantIdIn(allVariantIds);
                    int totalStock = allVariantSizes.stream().mapToInt(VariantSizeEntity::getStock).sum();
                    productRepository.findById(productId).ifPresent(p -> {
                        p.setTotalStock(totalStock);
                        productRepository.save(p);
                    });
                }
            }
        } else {
            // Xóa cả VariantEntity và tất cả VariantSizeEntity liên quan
            VariantEntity variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

            // Kiểm tra xem variant còn trong giỏ hàng không (nếu cartItem lưu VariantEntity ID)
            if (cartItemRepository.existsByVariant_Id(variantId)) {
                throw new AppException(ErrorCode.VARIANT_IN_CART);
            }

            // Lấy tất cả VariantSizeEntity của variant này
            List<VariantSizeEntity> variantSizes = variantSizeRepository.findByVariantId(variantId);
            
            // Kiểm tra xem có variantSize nào còn trong giỏ hàng không (nếu cartItem lưu VariantSizeEntity ID)
            for (VariantSizeEntity vs : variantSizes) {
                if (cartItemRepository.existsByVariant_Id(vs.getId())) {
                    throw new AppException(ErrorCode.VARIANT_IN_CART);
                }
            }

            // 1) Xóa history của tất cả variantSize
            List<String> variantSizeIds = variantSizes.stream()
                    .map(VariantSizeEntity::getId)
                    .toList();
            try {
                historyProductRepository.deleteByVariantSizeIdIn(variantSizeIds);
            } catch (Exception ignore) {
                // Fallback: xóa từng cái
                variantSizeIds.forEach(vsId -> {
                    historyProductRepository.findByVariantSizeId(vsId, PageRequest.of(0, Integer.MAX_VALUE))
                            .forEach(h -> historyProductRepository.deleteById(h.getId()));
                });
            }

            // 2) Xóa tất cả variantSize
            variantSizeRepository.deleteByVariantId(variantId);

            // 3) Xóa variant
            String productId = variant.getProductId();
            variantRepository.deleteById(variantId);

            // 4) Recalc product.totalStock
            List<VariantEntity> remain = variantRepository.findByProductId(productId);
            List<String> remainVariantIds = remain.stream()
                    .map(VariantEntity::getId)
                    .toList();
            List<VariantSizeEntity> remainVariantSizes = variantSizeRepository.findByVariantIdIn(remainVariantIds);
            int totalStock = remainVariantSizes.stream().mapToInt(VariantSizeEntity::getStock).sum();
            productRepository.findById(productId).ifPresent(p -> {
                p.setTotalStock(totalStock);
                productRepository.save(p);
            });
        }

        return true;
    }

    public VariantResponse getVariantById(String variantSizeId) {
        VariantSizeEntity variantSize = variantSizeRepository.findById(variantSizeId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));
        
        VariantEntity variant = variantRepository.findById(variantSize.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        return VariantResponse.builder()
                .id(variantSize.getId())
                .productId(variant.getProductId())
                .color(variant.getColor())
                .status(variant.getStatus())
                .size(variantSize.getSize())
                .stock(variantSize.getStock())
                .countSell(variantSize.getCountSell())
                .build();
    }

    public List<VariantResponse> getVariantsByProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        // Lấy tất cả VariantEntity của product
        List<VariantEntity> variants = variantRepository.findByProductId(productId);
        
        if (variants.isEmpty()) {
            return List.of();
        }

        // Lấy tất cả VariantSizeEntity của các variants
        List<String> variantIds = variants.stream()
                .map(VariantEntity::getId)
                .toList();
        
        List<VariantSizeEntity> variantSizes = variantSizeRepository.findByVariantIdIn(variantIds);

        // Map variants thành Map để truy cập nhanh
        Map<String, VariantEntity> variantMap = variants.stream()
                .collect(Collectors.toMap(VariantEntity::getId, v -> v));

        // Map VariantSizeEntity sang VariantResponse
        return variantSizes.stream()
                .map(vs -> {
                    VariantEntity variant = variantMap.get(vs.getVariantId());
                    if (variant == null) return null;
                    
                    return VariantResponse.builder()
                            .id(vs.getId())
                            .productId(variant.getProductId())
                            .color(variant.getColor())
                            .status(variant.getStatus())
                            .size(vs.getSize())
                            .stock(vs.getStock())
                            .countSell(vs.getCountSell())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public List<VariantGroupResponse> getVariantsGroupedByProductId(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        if (!productRepository.existsById(productId)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        // Lấy tất cả VariantEntity của product (group theo id và color)
        List<VariantEntity> variants = variantRepository.findByProductId(productId);
        
        if (variants.isEmpty()) {
            return List.of();
        }

        // Lấy tất cả VariantSizeEntity của các variants
        List<String> variantIds = variants.stream()
                .map(VariantEntity::getId)
                .toList();
        
        List<VariantSizeEntity> variantSizes = variantSizeRepository.findByVariantIdIn(variantIds);

        // Group VariantSizeEntity theo variantId
        Map<String, List<VariantSizeEntity>> sizesByVariantId = variantSizes.stream()
                .collect(Collectors.groupingBy(VariantSizeEntity::getVariantId));

        // Map sang VariantGroupResponse
        return variants.stream()
                .map(variant -> {
                    List<VariantSizeEntity> sizes = sizesByVariantId.getOrDefault(variant.getId(), List.of());
                    
                    List<VariantSizeResponse> sizeResponses = sizes.stream()
                            .map(vs -> com.java.shoes_service.dto.product.variant.VariantSizeResponse.builder()
                                    .id(vs.getId())
                                    .size(vs.getSize())
                                    .stock(vs.getStock())
                                    .countSell(vs.getCountSell())
                                    .build())
                            .toList();
                    
                    return VariantGroupResponse.builder()
                            .id(variant.getId())
                            .productId(variant.getProductId())
                            .color(variant.getColor())
                            .status(variant.getStatus())
                            .sizes(sizeResponses)
                            .build();
                })
                .toList();
    }
}