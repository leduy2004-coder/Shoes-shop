package com.java.shoes_service.repository.promotion;

import com.java.shoes_service.entity.promotion.CouponEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface CouponRepository extends MongoRepository<CouponEntity, String> {
    Optional<CouponEntity> findByCode(String code);
    
    Page<CouponEntity> findByCodeRegexIgnoreCase(String codeRegex, Pageable pageable);
    
    Page<CouponEntity> findByActive(boolean active, Pageable pageable);
    
    Page<CouponEntity> findByActiveAndExpirationDateAfter(boolean active, Instant expirationDate, Pageable pageable);
}

