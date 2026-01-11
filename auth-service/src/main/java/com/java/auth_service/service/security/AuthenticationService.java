package com.java.auth_service.service.security;

import com.java.IntrospectRequest;
import com.java.IntrospectResponse;
import com.java.auth_service.dto.request.AuthenticationRequest;
import com.java.auth_service.dto.request.ChangePassRequest;
import com.java.auth_service.dto.request.UserRequest;
import com.java.auth_service.dto.response.AuthenticationResponse;

import com.java.auth_service.dto.response.UserRegisterResponse;
import com.java.auth_service.entity.UserEntity;
import com.java.auth_service.exception.AppException;
import com.java.auth_service.exception.ErrorCode;
import com.java.auth_service.repository.UserRepository;
import com.java.auth_service.service.EmailService;
import com.java.auth_service.service.UserService;
import com.java.auth_service.service.impl.JwtService;
import com.java.auth_service.service.redis.TokenRedisService;

import com.java.auth_service.utility.enumUtils.OtpStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    JwtService jwtService;
    UserService userService;
    UserRepository userRepository;
    TokenRedisService tokenRedisService;
    ModelMapper modelMapper;
    EmailService emailService;

    @NonFinal
    @Value("${spring.application.security.jwt.secret-key}")
    protected String SIGNER_KEY;

    public IntrospectResponse introspect(IntrospectRequest request) {
        var token = request.getToken();
        boolean isValid = true;
        String id = null;
        try {
            id = verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).userId(id).build();
    }

    public Boolean register(UserRequest request) {
        userService.register(request);
        emailService.sendOtp(request.getEmail());
        return true;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) {
        var jwtToken = "";
        var refreshToken = "";

        UserEntity user;
        UserRegisterResponse userRegisterResponse;
        try {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
            user = userRepository.findByEmailAndStatusTrue(request.getEmail()).orElse(null);

            assert user != null;
            boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

            if (!authenticated)
                throw new AppException(ErrorCode.UNAUTHENTICATED);

            jwtToken = jwtService.generateToken(user);
            refreshToken = jwtService.generateRefreshToken(user);
            tokenRedisService.saveRefreshToken(user.getId(), refreshToken);
            // Set cookie
            setRefreshTokenCookie(response, refreshToken);

            userRegisterResponse = modelMapper.map(user, UserRegisterResponse.class);
            userRegisterResponse.setRole(user.getRole().getCode());

        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .user(userRegisterResponse)
                .build();
    }

    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        var refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            String userName;
            try {
                userName = verifyToken(refreshToken);
            } catch (AppException e) {
                log.error("Refresh token validation failed: {}", e.getMessage());
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            UserEntity user = userRepository.findById(userName).orElse(null);
            if (user == null)
                throw new AppException(ErrorCode.USER_NOT_EXISTED);

            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            tokenRedisService.saveRefreshToken(user.getId(), newRefreshToken);
            // Set cookie
            setRefreshTokenCookie(response, newRefreshToken);

            UserRegisterResponse userRegisterResponse = modelMapper.map(user, UserRegisterResponse.class);
            userRegisterResponse.setRole(user.getRole().getCode());

            return AuthenticationResponse.builder()
                    .user(userRegisterResponse)
                    .accessToken(newAccessToken)
                    .build();
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
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

    private String verifyToken(String token) {
        try {
            // Parse và xác minh token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtService.getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Kiểm tra hạn token
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            // Lấy username từ token
            String username = claims.getSubject();

            // Kiểm tra refresh token trong Redis
            if (tokenRedisService.getRefreshToken(username) == null) {
                throw new AppException(ErrorCode.UNAUTHENTICATED);
            }

            return username;

        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public Boolean changePassword(ChangePassRequest changePassRequest) {
        try {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

            String email = changePassRequest.getEmail();

            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            if (changePassRequest.getStatus() == OtpStatus.CHANGE_PASS) {

                boolean matched = passwordEncoder.matches(
                        changePassRequest.getPassword(),
                        user.getPassword());

                if (!matched) {
                    throw new AppException(ErrorCode.PASSWORD_WRONG);
                }

                if (passwordEncoder.matches(changePassRequest.getNewPass(), user.getPassword())) {
                    throw new AppException(ErrorCode.PASSWORD_NOT_SAME);
                }
            }
            user.setPassword(passwordEncoder.encode(changePassRequest.getNewPass()));
            userRepository.save(user);

            return true;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error when change password: ", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public boolean checkOTPRegister(String otp, String email, OtpStatus status) {
        boolean check = emailService.checkOTP(otp, email);
        if (check) {
            if (status == OtpStatus.REGISTER) {
                UserEntity user = userRepository.findByEmail(email).orElse(null);
                if (user == null)
                    throw new AppException(ErrorCode.USER_NOT_EXISTED);

                user.setStatus(true);
                userRepository.save(user);
            }
            return true;
        }
        return false;
    }

    public boolean sendOtp(String email) {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null)
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        emailService.sendOtp(email);
        return true;
    }

}
