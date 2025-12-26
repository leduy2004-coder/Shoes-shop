package com.java.shoes_service.repository.product;


import com.java.shoes_service.entity.product.VariantEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface VariantRepository extends MongoRepository<VariantEntity, String> {
    List<VariantEntity> findByProductId(String productId);
    
    Optional<VariantEntity> findByProductIdAndColorIgnoreCase(String productId, String color);
    
    boolean existsByProductIdAndColorIgnoreCase(String productId, String color);

    List<VariantEntity> findByProductIdIn(List<String> productIds);
}
