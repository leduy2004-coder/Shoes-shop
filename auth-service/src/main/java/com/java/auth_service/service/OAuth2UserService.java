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
    public AuthenticationResponse loginGoogle(String code) {

        String accessToken = exchangeGoogleToken(code);

        Oauth2UserResponse.GoogleUserInfo googleUser = googleUserInfoClient.getUserInfo(accessToken);

        UserEntity user = loadOrCreateGoogleUser(googleUser);

        return loginOauth2(user);
    }

    // ===================== GOOGLE TOKEN =====================
    private String exchangeGoogleToken(String code) {
        // Decode URL encoded code if needed (Spring usually does this automatically, but ensure it's decoded)
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
        
        ExchangeTokenResponse.ExchangeTokenGoogle response =
                googleIdentityClient.exchangeToken(request);

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
                .password(UUID.randomUUID().toString())
                .status(true)
                .role(role)
                .build();


        newUser = userRepository.save(newUser);

        return newUser;
    }

    // ===================== JWT =====================
    private AuthenticationResponse loginOauth2(UserEntity user) {

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        tokenRedisService.saveRefreshToken(user.getEmail(), refreshToken);
        UserRegisterResponse userRegisterResponse = modelMapper.map(user, UserRegisterResponse.class);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userRegisterResponse)
                .build();
    }
}
