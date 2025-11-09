package com.java.shoes_service.repository;

import com.java.shoes_service.entity.cart.CartItemEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends MongoRepository<CartItemEntity, String> {
    List<CartItemEntity> findByCartId(String cartId);
    void deleteByCartId(String cartId);
    boolean existsByVariant_Id(String variantId);
}

