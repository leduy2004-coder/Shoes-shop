package com.java.file_service.repository;


import com.java.file_service.entity.BrandImageEntity;
import com.java.file_service.entity.ProductImageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends MongoRepository<BrandImageEntity, String> {
    List<BrandImageEntity> findAllByBrandId(String brandId);

}
