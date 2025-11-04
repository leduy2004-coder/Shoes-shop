package com.java.auth_service.service;
import com.java.ProfileGetResponse;
import com.java.auth_service.dto.request.UserRequest;
import com.java.auth_service.dto.response.UserResponse;
import com.java.auth_service.entity.UserEntity;

import java.util.List;

public interface UserService {
    UserEntity insert(UserRequest userDto);
    Boolean delete(String id);
    UserResponse findById(String id);
    ProfileGetResponse getProfile(String id);
    List<UserResponse> findAll();
    UserResponse findByUsername(String userName);
    UserResponse updateUser(UserRequest userRequest);



}
