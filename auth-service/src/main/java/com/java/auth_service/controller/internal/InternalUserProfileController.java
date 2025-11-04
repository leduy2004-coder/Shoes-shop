package com.java.auth_service.controller.internal;


import com.java.ProfileGetResponse;
import com.java.auth_service.dto.ApiResponse;
import com.java.auth_service.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalUserProfileController {

    UserService userService;

    @GetMapping("/internal/users/{userId}")
    ApiResponse<ProfileGetResponse> getProfile(@PathVariable String userId) {
        return ApiResponse.<ProfileGetResponse>builder()
                .result(userService.getProfile(userId))
                .build();
    }
}