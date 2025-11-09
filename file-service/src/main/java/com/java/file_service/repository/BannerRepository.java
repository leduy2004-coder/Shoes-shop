package com.java.file_service.repository;


import com.java.file_service.entity.BannerImageEntity;
import com.java.file_service.entity.ProductImageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends MongoRepository<BannerImageEntity, String> {
    List<BannerImageEntity> findAllByBannerId(String bannerId);
    BannerImageEntity findByName(String name);
}
