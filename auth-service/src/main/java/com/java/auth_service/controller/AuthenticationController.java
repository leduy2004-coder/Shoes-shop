package com.java.auth_service.controller;

import com.java.IntrospectRequest;
import com.java.IntrospectResponse;
import com.java.auth_service.dto.ApiResponse;
import com.java.auth_service.dto.request.AuthenticationRequest;
import com.java.auth_service.dto.response.AuthenticationResponse;
import com.java.auth_service.service.security.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/auth2")
public class AuthenticationController {

    AuthenticationService service;


    @PostMapping("/authenticate")
    public ApiResponse<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ApiResponse.<AuthenticationResponse>builder().result(service.authenticate(request)).build();
    }
    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        var result = service.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }
    @PostMapping("/refresh-token")
    public ApiResponse<AuthenticationResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        return ApiResponse.<AuthenticationResponse>builder().result(service.refreshToken(request, response)).build();
    }

}