package com.java.shoes_service.service.product;

import com.java.shoes_service.dto.PageResponse;
import com.java.shoes_service.dto.product.product.ProductGetResponse;
import com.java.shoes_service.dto.product.variant.*;
import com.java.shoes_service.entity.product.HistoryProductEntity;
import com.java.shoes_service.entity.product.ProductEntity;
import com.java.shoes_service.entity.product.SizeLabel;
import com.java.shoes_service.entity.product.VariantEntity;
import com.java.shoes_service.exception.AppException;
import com.java.shoes_service.exception.ErrorCode;
import com.java.shoes_service.repository.CartItemRepository;
import com.java.shoes_service.repository.product.HistoryProductRepository;
import com.java.shoes_service.repository.product.ProductRepository;
import com.java.shoes_service.repository.product.VariantRepository;
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
    ProductRepository productRepository;
    HistoryProductRepository historyProductRepository;
    ModelMapper modelMapper;
    CartItemRepository cartItemRepository;

    public List<VariantResponse> createVariant(VariantCreateRequest request) {
        try {
            if (request == null || request.getProductId() == null || request.getProductId().isBlank()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            if (request.getVariants() == null || request.getVariants().isEmpty()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            ProductEntity product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

            // Tạo các variant MỚI (stock mặc định 0, countSell 0)
            List<VariantResponse> created = new ArrayList<>();
            for (VariantRequest vReq : request.getVariants()) {
                String color = vReq.getColor();
                String sizeLabelStr = String.valueOf(vReq.getSize());

                // Chặn trùng (productId + color + size)
                boolean exists = variantRepository.existsByProductIdAndColorIgnoreCaseAndSize_Label(
                        product.getId(), color, sizeLabelStr
                );
                if (exists) {
                    // throw new AppException(ErrorCode.VARIANT_DUPLICATED);
                    continue;
                }

                VariantEntity ve = new VariantEntity();
                ve.setProductId(product.getId());
                ve.setColor(color);
                ve.setStatus(ProductStatus.ACTIVE);
                ve.setCountSell(0);
                ve.setStock(0);

                SizeLabel size = SizeLabel.builder()
                        .label(sizeLabelStr)
                        .build();
                ve.setSize(size);

                VariantEntity saved = variantRepository.save(ve);
                created.add(modelMapper.map(saved, VariantResponse.class));
            }

            return created;

        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
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

        // Lấy tất cả variant cần cập nhật 1 lần
        List<String> ids = req.getItems().stream()
                .map(VariantStockImportItem::getVariantId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) throw new AppException(ErrorCode.INVALID_REQUEST);

        Map<String, VariantEntity> variantMap = variantRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(VariantEntity::getId, v -> v));

        List<VariantEntity> toUpdate = new ArrayList<>();
        List<HistoryProductEntity> histories = new ArrayList<>();

        // 2) Cập nhật từng item (cộng/trừ tồn), ghi history
        for (VariantStockImportItem it : req.getItems()) {
            if (it.getVariantId() == null || it.getVariantId().isBlank()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            VariantEntity v = variantMap.get(it.getVariantId());
            if (v == null) throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
            if (!req.getProductId().equals(v.getProductId()))
                throw new AppException(ErrorCode.INVALID_REQUEST); // variant không thuộc product

            if (it.getCount() == 0) continue; // bỏ qua delta = 0

            long newStock = (long) v.getStock() + it.getCount();
            if (newStock < 0) throw new AppException(ErrorCode.INVALID_REQUEST); // không cho âm

            v.setStock((int) newStock);
            toUpdate.add(v);

            HistoryProductEntity h = new HistoryProductEntity();
            h.setVariantId(v.getId());
            h.setCount(it.getCount()); // dương: nhập, âm: xuất/giảm
            histories.add(h);
        }

        if (!toUpdate.isEmpty()) variantRepository.saveAll(toUpdate);
        if (!histories.isEmpty()) historyProductRepository.saveAll(histories);

        // 3) Recalc totalStock = tổng stock của tất cả variants thuộc product
        List<VariantEntity> all = variantRepository.findByProductId(req.getProductId());
        int totalStock = all.stream().mapToInt(VariantEntity::getStock).sum();
        productRepository.findById(req.getProductId()).ifPresent(p -> {
            p.setTotalStock(totalStock);
            productRepository.save(p);
        });

        // 4) Trả về các variant đã cập nhật (nếu muốn trả toàn bộ thì map `all` thay vì `toUpdate`)
        return toUpdate.stream()
                .map(v -> modelMapper.map(v, VariantResponse.class))
                .toList();
    }

    public PageResponse<VariantHistoryResponse> getHistory(String variantId, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page - 1),
                Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "createdDate")
        );

        Page<HistoryProductEntity> p = (variantId == null || variantId.isBlank())
                ? historyProductRepository.findAll(pageable)
                : historyProductRepository.findByVariantId(variantId, pageable);

        // --- Batch load variants ---
        List<String> variantIds = p.getContent().stream()
                .map(HistoryProductEntity::getVariantId)
                .filter(Objects::nonNull)
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
                    VariantEntity v = variantMap.get(h.getVariantId());
                    ProductEntity prod = (v != null) ? productMap.get(v.getProductId()) : null;

                    VariantHistoryResponse.VariantHistoryResponseBuilder b = VariantHistoryResponse.builder()
                            .id(h.getId())
                            .count(h.getCount());

                    if (v != null) {
                        b.variant(modelMapper.map(v, VariantResponse.class))
                                .color(v.getColor())
                                .size(v.getSize());
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
        VariantEntity variant = variantRepository.findById(request.getId()).orElse(null);
        if (variant == null) throw new AppException(ErrorCode.VARIANT_NOT_FOUND);
        variant.setColor(request.getColor());
        SizeLabel size = SizeLabel.builder()
                .label(String.valueOf(request.getSize()))
                .build();
        variant.setSize(size);

        VariantEntity saved = variantRepository.save(variant);
        return modelMapper.map(saved, VariantResponse.class);
    }
    @Transactional
    public boolean deleteVariant(String variantId) {
        VariantEntity variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new AppException(ErrorCode.VARIANT_NOT_FOUND));

        //  Nếu vẫn còn trong giỏ hàng -> không cho xoá
        if (cartItemRepository.existsByVariant_Id(variantId)) {
            throw new AppException(ErrorCode.VARIANT_IN_CART);
        }

        // Không còn cartItem nào tham chiếu -> xoá an toàn

        // 1) Xoá history của variant (nếu có)
        try {
            historyProductRepository.deleteByVariantId(variantId);
        } catch (Exception ignore) {
            historyProductRepository.findByVariantId(variantId, PageRequest.of(0, Integer.MAX_VALUE))
                    .forEach(h -> historyProductRepository.deleteById(h.getId()));
        }

        // 2) Xoá variant
        String productId = variant.getProductId();
        variantRepository.deleteById(variantId);

        // 3) Recalc product.totalStock
        List<VariantEntity> remain = variantRepository.findByProductId(productId);
        int totalStock = remain.stream().mapToInt(VariantEntity::getStock).sum();
        productRepository.findById(productId).ifPresent(p -> {
            p.setTotalStock(totalStock);
            productRepository.save(p);
        });

        return true;
    }
}