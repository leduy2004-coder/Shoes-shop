package com.java.shoes_service.repository.product;
import com.java.shoes_service.entity.order.UserVariantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UserVariantRepository extends MongoRepository<UserVariantEntity, String> {
    List<UserVariantEntity> findByUserId(String userId);
    Page<UserVariantEntity> findByUserId(String userId, Pageable pageable);
    List<UserVariantEntity> findByCreatedDateBetween(Instant from, Instant to);

    List<UserVariantEntity> findTop10ByOrderByCreatedDateDesc();
    
    List<UserVariantEntity> findByVariantSizeIdIn(List<String> variantSizeIds);
    Page<UserVariantEntity> findByVariantSizeIdIn(List<String> variantSizeIds, Pageable pageable);
    
    List<UserVariantEntity> findByOrderId(String orderId);

}