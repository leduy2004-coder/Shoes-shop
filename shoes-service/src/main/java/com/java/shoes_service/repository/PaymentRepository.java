package com.java.shoes_service.repository;

import com.java.shoes_service.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends MongoRepository<PaymentEntity, String> {
    Page<PaymentEntity> findAllByUserId(String userId, Pageable pageable);

}
