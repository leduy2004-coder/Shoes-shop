package com.java.file_service.repository;


import com.java.file_service.entity.ProductImageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<ProductImageEntity, String> {
     List<ProductImageEntity> findAllByProductId(String productId);
}
