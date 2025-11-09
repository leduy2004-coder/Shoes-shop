package com.java.shoes_service.repository.product;


import com.java.shoes_service.entity.product.VariantEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface VariantRepository extends MongoRepository<VariantEntity, String> {
    List<VariantEntity> findByProductId(String productId);
    boolean existsByProductIdAndColorIgnoreCaseAndSize_Label(String productId, String color, String sizeLabel);
}
