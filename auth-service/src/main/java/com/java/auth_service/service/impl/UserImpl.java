package com.java.auth_service.service.impl;

import com.java.auth_service.dto.PageResponse;
import com.java.auth_service.dto.request.UserRequest;
import com.java.auth_service.dto.request.UserUpdateRequest;
import com.java.auth_service.dto.response.UserResponse;
import com.java.auth_service.entity.RoleEntity;
import com.java.auth_service.entity.UserEntity;
import com.java.auth_service.exception.AppException;
import com.java.auth_service.exception.ErrorCode;
import com.java.auth_service.repository.RoleRepository;
import com.java.auth_service.repository.UserRepository;
import com.java.auth_service.service.RoleService;
import com.java.auth_service.service.UserService;
import com.java.auth_service.utility.GetInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserImpl implements UserService {
   UserRepository userRepository;
   ModelMapper modelMapper;
   RoleService roleService;
   PasswordEncoder passwordEncoder;
   RoleRepository roleRepository;
   MongoTemplate mongoTemplate;


    @Override
    public UserEntity register(UserRequest userRequest) {
       return insert(userRequest, "USER", false);
    }

    public UserEntity insert(UserRequest userRequest, String role, Boolean isAdmin) {
        if (userRepository.findByEmail(userRequest.getEmail()).isPresent())
            throw new AppException(ErrorCode.USER_EXISTED);

        UserEntity userEntity = modelMapper.map(userRequest, UserEntity.class);
        userEntity.setRole(modelMapper.map(roleService.findByCode(role), RoleEntity.class));
        userEntity.setStatus(false);
        if (isAdmin) userEntity.setStatus(true);
        userEntity.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        return userRepository.save(userEntity);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse addUser(UserRequest userRequest) {
        return modelMapper.map(insert(userRequest, userRequest.getRole().getCode(), true), UserResponse.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public Boolean delete(List<String> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return false;
            }
            List<UserEntity> users = userRepository.findAllById(ids);
            if (users.isEmpty()) {
                return false;
            }

            userRepository.deleteAll(users);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public UserResponse findById(String id) {
        UserEntity user = userRepository.findByIdAndStatusTrue(id)
                .orElse(null);

        if (user == null) {
            return null;
        }

        return modelMapper.map(user, UserResponse.class);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<UserResponse> findAll() {
        log.info("In method in admin");
        List<UserEntity> list = userRepository.findAll();
        return mapUserEntitiesToResponses(list);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public PageResponse<UserResponse> findAllWithPagination(int page, int size, String name, String email) {
        // Build criteria for search
        List<Criteria> ands = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            // Case-insensitive regex search for name
            ands.add(Criteria.where("name").regex(name.trim(), "i"));
        }

        if (email != null && !email.isBlank()) {
            // Case-insensitive regex search for email
            ands.add(Criteria.where("email").regex(email.trim(), "i"));
        }

        Query query = new Query();
        if (!ands.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(ands.toArray(Criteria[]::new)));
        }

        // Sort by createdDate descending
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        query.with(sort);

        // Paging (controller 1-based)
        int pageIndex = Math.max(0, page - 1);
        int pageSize = Math.max(1, size);
        query.skip((long) pageIndex * pageSize).limit(pageSize);

        // Execute query
        List<UserEntity> content = mongoTemplate.find(query, UserEntity.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), UserEntity.class);

        // Map to response
        List<UserResponse> items = mapUserEntitiesToResponses(content);

        int totalPages = (int) Math.ceil((double) total / pageSize);

        return new PageResponse<>(
                page,
                pageSize,
                total,
                totalPages,
                items
        );
    }

    @Override
    public UserResponse updateUser(UserUpdateRequest userRequest) {
        UserEntity userEntity = userRepository.findById(userRequest.getId()).orElseThrow();
        if (GetInfo.isAdmin() && userRequest.getStatus() != null){
            userEntity.setStatus(userRequest.getStatus());
        }
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

    @Override
    public UserResponse findAdmin() {
        RoleEntity adminRole = roleRepository.findByCode("ADMIN")
                .orElseThrow(() -> new RuntimeException("Role ADMIN not found"));

        UserEntity adminUser = userRepository
                .findFirstByRoleId(adminRole.getId())
                .orElse(null);

        return modelMapper.map(adminUser, UserResponse.class);
    }
}