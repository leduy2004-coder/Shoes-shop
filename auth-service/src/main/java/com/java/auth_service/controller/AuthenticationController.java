package com.java.auth_service.controller;

import com.java.IntrospectRequest;
import com.java.IntrospectResponse;
import com.java.auth_service.dto.ApiResponse;
import com.java.auth_service.dto.request.AuthenticationRequest;
import com.java.auth_service.dto.request.ChangePassRequest;
import com.java.auth_service.dto.request.UserRequest;
import com.java.auth_service.dto.request.VerifyAccount;
import com.java.auth_service.dto.response.AuthenticationResponse;
import com.java.auth_service.service.security.AuthenticationService;
import com.java.auth_service.utility.enumUtils.OtpStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService service;

    @PostMapping("/register")
    public ApiResponse<Boolean> register(
            @RequestBody UserRequest request
    ) {
        return ApiResponse.<Boolean>builder().result(service.register(request)).build();
    }

    @PostMapping("/login")
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
    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        return ApiResponse.<AuthenticationResponse>builder().result(service.refreshToken(request, response)).build();
    }
    @PostMapping("/change-pass")
    public ApiResponse<Boolean> changePassForget(@RequestBody ChangePassRequest request) {
        return ApiResponse.<Boolean>builder().result(service.changePassword(request)).build();
    }
    @PostMapping("/email/verify-account")
    public ApiResponse<Boolean> verifyAccount(@RequestBody VerifyAccount request) {
        Boolean check = service.checkOTPRegister(request.getOtp(), request.getEmail(), request.getStatus());
        return ApiResponse.<Boolean>builder().result(check).build();
    }

    @PostMapping("/email/generate-otp")
    public ApiResponse<Boolean> generateOtp(@RequestParam(value = "email") String email) {
        return ApiResponse.<Boolean>builder().result(service.sendOtp(email)).build();
    }
}