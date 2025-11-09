package com.java.shoes_service.repository.product;

import com.java.shoes_service.entity.product.HistoryProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryProductRepository extends MongoRepository<HistoryProductEntity, String> {
    Page<HistoryProductEntity> findByVariantId(String variantId, Pageable pageable);
    void deleteByVariantIdIn(List<String> variantIds);
    List<HistoryProductEntity> findAllByVariantIdIn(List<String> variantIds);
    void deleteByVariantId(String variantId);
}
