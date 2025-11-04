package com.java.auth_service.service;


import com.java.auth_service.dto.request.RoleRequest;
import com.java.auth_service.dto.response.RoleResponse;

public interface RoleService {
    RoleResponse insert(RoleRequest roleDto);
    RoleResponse findByCode(String code);
}
