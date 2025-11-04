package com.java.auth_service.repository;


import com.java.auth_service.entity.RoleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends MongoRepository<RoleEntity, String> {
     Optional<RoleEntity> findByCode(String code);
}
