package com.java.shoes_service.repository.promotion;


import com.java.shoes_service.entity.promotion.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReviewRepository extends MongoRepository<ReviewEntity, String> {
    Page<ReviewEntity> findByProductId(String productId, Pageable pageable);
    long countByProductId(String productId);
    @Query(value = "{ 'productId': ?0 }", fields = "{ 'rating': 1 }")
    List<ReviewEntity> findRatingsByProductId(String productId);
    long deleteByProductId(String productId);
}
