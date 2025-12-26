package com.java.shoes_service.repository.product;


import com.java.shoes_service.entity.product.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, String> {
    long countByCategory_Id(String categoryId);
    List<ProductEntity> findByNameContainingIgnoreCase(String name);
    long countByBrand_Id(String brandId);

}

