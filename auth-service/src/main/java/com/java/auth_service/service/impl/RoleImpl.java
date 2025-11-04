package com.java.auth_service.service.impl;

import com.java.auth_service.dto.request.RoleRequest;
import com.java.auth_service.dto.response.RoleResponse;
import com.java.auth_service.entity.RoleEntity;
import com.java.auth_service.exception.AppException;
import com.java.auth_service.exception.ErrorCode;
import com.java.auth_service.repository.RoleRepository;
import com.java.auth_service.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleImpl implements RoleService {
    RoleRepository repository;
    ModelMapper modelMapper;

    @Override
    @Transactional
    public RoleResponse insert(RoleRequest roleDto) {
        return modelMapper.map(repository.save(modelMapper.map(roleDto, RoleEntity.class)), RoleResponse.class);
    }

    @Override
    public RoleResponse findByCode(String code) {
        RoleEntity role = repository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return modelMapper.map(role,RoleResponse.class);
    }

}
