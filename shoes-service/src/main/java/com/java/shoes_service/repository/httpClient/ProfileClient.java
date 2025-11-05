package com.java.shoes_service.repository.httpClient;

import com.java.ProfileGetResponse;
import com.java.shoes_service.config.security.AuthenticationRequestInterceptor;
import com.java.shoes_service.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service",
        configuration = {AuthenticationRequestInterceptor.class})
public interface ProfileClient {
    @GetMapping("/auth/internal/users/{userId}")
    ApiResponse<ProfileGetResponse> getProfile(@PathVariable String userId);
}

