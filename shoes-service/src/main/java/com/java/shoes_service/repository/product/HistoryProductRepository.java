package com.java.shoes_service.repository.product;

import com.java.shoes_service.entity.product.HistoryProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryProductRepository extends MongoRepository<HistoryProductEntity, String> {
    Page<HistoryProductEntity> findByVariantSizeId(String variantSizeId, Pageable pageable);
    void deleteByVariantSizeIdIn(List<String> variantSizeIds);
    List<HistoryProductEntity> findAllByVariantSizeIdIn(List<String> variantSizeIds);
    void deleteByVariantSizeId(String variantSizeId);
}
