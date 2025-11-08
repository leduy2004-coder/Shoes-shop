package com.java.shoes_service.repository;


import com.java.shoes_service.entity.brand.BrandEntity;
import com.java.shoes_service.entity.product.CategoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface BrandRepository extends MongoRepository<BrandEntity, String> {
}
