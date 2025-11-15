package com.java.shoes_service.repository.product;


import com.java.shoes_service.entity.product.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, String> {
    long countByCategory_Id(String categoryId);

    boolean existsByCategory_Id(String categoryId);
}
