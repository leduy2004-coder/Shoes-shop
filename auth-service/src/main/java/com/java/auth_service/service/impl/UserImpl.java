package com.java.auth_service.service.impl;

import com.java.auth_service.dto.request.UserRequest;
import com.java.auth_service.dto.request.UserUpdateRequest;
import com.java.auth_service.dto.response.UserResponse;
import com.java.auth_service.entity.RoleEntity;
import com.java.auth_service.entity.UserEntity;
import com.java.auth_service.exception.AppException;
import com.java.auth_service.exception.ErrorCode;
import com.java.auth_service.repository.UserRepository;
import com.java.auth_service.service.RoleService;
import com.java.auth_service.service.UserService;
import com.java.auth_service.utility.GetInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserImpl implements UserService {
   UserRepository userRepository;
   ModelMapper modelMapper;
   RoleService roleService;
   PasswordEncoder passwordEncoder;


    @Override
    public UserEntity register(UserRequest userRequest) {
       return insert(userRequest, "User");
    }

    public UserEntity insert(UserRequest userRequest, String role) {
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent())
            throw new AppException(ErrorCode.USER_EXISTED);

        UserEntity userEntity = modelMapper.map(userRequest, UserEntity.class);
        userEntity.setRole(modelMapper.map(roleService.findByCode(role), RoleEntity.class));
        userEntity.setStatus(true);
        userEntity.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        return userRepository.save(userEntity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse addUser(UserRequest userRequest) {
        return modelMapper.map(insert(userRequest, userRequest.getRole().getCode()), UserResponse.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public Boolean delete(String id) {
        try {
            Optional<UserEntity> entity = userRepository.findById(id);
            if (entity.isPresent()) {
                userRepository.deleteById(id);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public UserResponse findById(String id) {
        UserEntity user = userRepository.findById(id)
                .orElse(null);
        if (user == null) {
            return null;
        }
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);
        return userResponse;
    }


    @Override
    public UserResponse findByEmail(String userName) {
        UserEntity user = userRepository.findByEmail(userName)
                .orElse(null);
        if (user == null) {
            return null;
        }
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);

        return userResponse;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<UserResponse> findAll() {
        log.info("In method in admin");
        List<UserEntity> list = userRepository.findAll();
        return mapUserEntitiesToResponses(list);
    }


    @Override
    public UserResponse updateUser(UserUpdateRequest userRequest) {
        UserEntity userEntity = userRepository.findById(userRequest.getId()).orElseThrow();
        userEntity.setStatus(userRequest.getStatus());
        userEntity.setAddress(userRequest.getAddress());
        userEntity.setPhone(userRequest.getPhone());
        userEntity.setName(userRequest.getName());
        UserEntity user = userRepository.save(userEntity);
        return modelMapper.map(user, UserResponse.class);
    }


    public List<UserResponse> mapUserEntitiesToResponses(List<UserEntity> userEntities) {
        return userEntities.stream()
                .map(userEntity -> {
                    return modelMapper.map(userEntity, UserResponse.class);
                })
                .toList();
    }


}