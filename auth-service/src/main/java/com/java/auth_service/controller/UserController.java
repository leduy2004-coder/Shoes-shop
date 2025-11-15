package com.java.auth_service.controller;


import com.java.auth_service.dto.ApiResponse;
import com.java.auth_service.dto.request.UserRequest;
import com.java.auth_service.dto.request.UserUpdateRequest;
import com.java.auth_service.dto.response.UserResponse;
import com.java.auth_service.service.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/users")
public class UserController {
    UserService userService;

    @GetMapping("/get-all")
    public ApiResponse<List<UserResponse>> findAll() {
        List<UserResponse> list = userService.findAll();
        return ApiResponse.<List<UserResponse>>builder().result(list).build();
    }

    @GetMapping("/get-user")
    public ApiResponse<UserResponse> getUser(@RequestParam(value = "id") String id) {
        UserResponse userResponse = userService.findById(id);
        return ApiResponse.<UserResponse>builder().result(userResponse).build();
    }

    @PostMapping("/add-user")
    public ApiResponse<UserResponse> register(
            @RequestBody UserRequest request
    ) {
        return ApiResponse.<UserResponse>builder().result(userService.addUser(request)).build();
    }

    @PostMapping("/update-user")
    public ApiResponse<UserResponse> update(@RequestBody UserUpdateRequest request) {
        return ApiResponse.<UserResponse>builder().result(userService.updateUser(request)).build();
    }

    @DeleteMapping("/delete-user")
    public ApiResponse<Boolean> deleteUser(@RequestBody List<String> ids) {
        Boolean status = userService.delete(ids);
        return ApiResponse.<Boolean>builder().result(status).build();
    }

}
