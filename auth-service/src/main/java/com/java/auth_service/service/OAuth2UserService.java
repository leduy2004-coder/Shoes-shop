package com.java.auth_service.service;

import com.java.auth_service.dto.response.AuthenticationResponse;
import com.java.auth_service.dto.response.ExchangeTokenResponse;
import com.java.auth_service.dto.response.Oauth2UserResponse;
import com.java.auth_service.dto.response.UserRegisterResponse;
import com.java.auth_service.entity.RoleEntity;
import com.java.auth_service.entity.UserEntity;
import com.java.auth_service.repository.RoleRepository;
import com.java.auth_service.repository.UserRepository;
import com.java.auth_service.repository.feignClient.GoogleIdentityClient;
import com.java.auth_service.repository.feignClient.GoogleUserInfoClient;
import com.java.auth_service.service.impl.JwtService;
import com.java.auth_service.service.redis.TokenRedisService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OAuth2UserService {

    ModelMapper modelMapper;
    JwtService jwtService;
    TokenRedisService tokenRedisService;
    UserRepository userRepository;
    GoogleIdentityClient googleIdentityClient;
    GoogleUserInfoClient googleUserInfoClient;
    RoleRepository roleRepository;

    @NonFinal
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    protected String clientId;

    @NonFinal
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    protected String clientSecret;

    @NonFinal
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    protected String redirectUri;

    static final String GRANT_TYPE = "authorization_code";

    // ===================== LOGIN GOOGLE =====================
    // ===================== LOGIN GOOGLE =====================
    public AuthenticationResponse loginGoogle(String code, HttpServletResponse response) {

        String accessToken = exchangeGoogleToken(code);

        Oauth2UserResponse.GoogleUserInfo googleUser = googleUserInfoClient.getUserInfo(accessToken);

        UserEntity user = loadOrCreateGoogleUser(googleUser);

        return loginOauth2(user, response);
    }

    // ===================== GOOGLE TOKEN =====================
    private String exchangeGoogleToken(String code) {
        // Decode URL encoded code if needed (Spring usually does this automatically,
        // but ensure it's decoded)
        String decodedCode = code;
        try {
            // Try to decode - if it's already decoded, this will return the same value
            decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed to decode code, using original: {}", e.getMessage());
        }

        log.info("Original code: {}", code);
        log.info("Decoded code: {}", decodedCode);
        log.info("Redirect URI: {}", redirectUri);
        log.info("Client ID: {}", clientId);

        Map<String, String> request = new HashMap<>();
        request.put("code", decodedCode);
        request.put("client_id", clientId);
        request.put("client_secret", clientSecret);
        request.put("redirect_uri", redirectUri);
        request.put("grant_type", GRANT_TYPE);

        ExchangeTokenResponse.ExchangeTokenGoogle response = googleIdentityClient.exchangeToken(request);

        log.info("Token exchange successful");
        return response.getAccessToken();
    }

    // ===================== USER =====================
    private UserEntity loadOrCreateGoogleUser(Oauth2UserResponse.GoogleUserInfo googleUser) {

        String email = googleUser.getEmail();

        UserEntity user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            tokenRedisService.clearByUserName(email);
            return user;
        }

        RoleEntity role = roleRepository.findByCode("USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        UserEntity newUser = UserEntity.builder()
                .email(email)
                .password("")
                .name(googleUser.getName())
                .status(true)
                .role(role)
                .build();

        newUser = userRepository.save(newUser);

        return newUser;
    }

    // ===================== JWT =====================
    // ===================== JWT =====================
    private AuthenticationResponse loginOauth2(UserEntity user, HttpServletResponse response) {

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        tokenRedisService.saveRefreshToken(user.getId(), refreshToken);

        // Set cookie
        setRefreshTokenCookie(response, refreshToken);

        UserRegisterResponse userRegisterResponse = modelMapper.map(user, UserRegisterResponse.class);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .user(userRegisterResponse)
                .build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
