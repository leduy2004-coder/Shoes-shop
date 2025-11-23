package com.java.shoes_service.repository;

import com.java.shoes_service.entity.shipping.UserAddress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserAddressRepository extends MongoRepository<UserAddress, String> {
    List<UserAddress> findByUserId(String userId);
}
