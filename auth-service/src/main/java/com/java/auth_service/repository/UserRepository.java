package com.java.auth_service.repository;


import com.java.auth_service.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {


    Optional<UserEntity> findById(String id);

    Optional<UserEntity> findByUsername(String username);

//    @Query("UPDATE UserEntity u SET u.authType = ?2 WHERE u.username = ?1")
//    void updateAuthenticationType(String username, Resource.AuthenticationType authType);

}
