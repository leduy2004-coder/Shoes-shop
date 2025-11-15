package com.java.shoes_service.repository.product;
import com.java.shoes_service.entity.product.UserVariantEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVariantRepository extends MongoRepository<UserVariantEntity, String> {
    List<UserVariantEntity> findByUserId(String userId);
}