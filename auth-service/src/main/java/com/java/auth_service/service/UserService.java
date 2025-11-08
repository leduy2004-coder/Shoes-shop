package com.java.auth_service.service;

import com.java.auth_service.dto.request.UserRequest;
import com.java.auth_service.dto.request.UserUpdateRequest;
import com.java.auth_service.dto.response.UserResponse;
import com.java.auth_service.entity.UserEntity;

import java.util.List;

public interface UserService {
    UserEntity insert(UserRequest userDto);
    Boolean delete(String id);
    UserResponse findById(String id);
    List<UserResponse> findAll();
    UserResponse findByEmail(String userName);
    UserResponse updateUser(UserUpdateRequest userRequest);



}
