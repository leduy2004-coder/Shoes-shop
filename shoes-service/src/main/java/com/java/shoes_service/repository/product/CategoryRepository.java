package com.java.shoes_service.repository.product;


import com.java.shoes_service.entity.product.CategoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepository extends MongoRepository<CategoryEntity, String> {

}
