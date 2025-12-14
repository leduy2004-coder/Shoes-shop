package com.java.shoes_service.repository.product;

import com.java.shoes_service.entity.product.VariantSizeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantSizeRepository extends MongoRepository<VariantSizeEntity, String> {
    List<VariantSizeEntity> findByVariantId(String variantId);
    
    List<VariantSizeEntity> findByVariantIdIn(List<String> variantIds);
    
    boolean existsByVariantIdAndSize(String variantId, String size);
    
    void deleteByVariantId(String variantId);
}

