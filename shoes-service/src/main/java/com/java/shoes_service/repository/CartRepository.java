package com.java.shoes_service.repository;

import com.java.shoes_service.entity.cart.CartEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<CartEntity, String> {
    Optional<CartEntity> findByUserId(String userId);
}

