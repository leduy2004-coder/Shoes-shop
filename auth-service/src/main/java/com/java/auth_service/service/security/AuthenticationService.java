package com.java.auth_service.service.security;

import com.java.IntrospectRequest;
import com.java.IntrospectResponse;
import com.java.auth_service.dto.request.AuthenticationRequest;
import com.java.auth_service.dto.request.ChangePassRequest;
import com.java.auth_service.dto.request.UserRequest;
import com.java.auth_service.dto.response.AuthenticationResponse;
import com.java.auth_service.dto.response.RoleResponse;
import com.java.auth_service.dto.response.UserRegisterResponse;
import com.java.auth_service.entity.UserEntity;
import com.java.auth_service.exception.AppException;
import com.java.auth_service.exception.ErrorCode;
import com.java.auth_service.repository.UserRepository;
import com.java.auth_service.service.EmailService;
import com.java.auth_service.service.UserService;
import com.java.auth_service.service.impl.JwtService;
import com.java.auth_service.service.redis.TokenRedisService;
import com.java.auth_service.utility.GetInfo;
import com.java.auth_service.utility.enumUtils.OtpStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public Boolean register(UserRequest request) {
        userService.register(request);
        emailService.sendOtp(request.getEmail());
        return true;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var jwtToken = "";
        var refreshToken = "";
        List<RoleResponse> roles;
        UserEntity user;
        UserRegisterResponse userRegisterResponse;
        try {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
            user = userRepository.findByEmailAndStatusTrue(request.getEmail()).orElse(null);

            assert user != null;
            boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

            if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

            jwtToken = jwtService.generateToken(user);
            refreshToken = jwtService.generateRefreshToken(user);
            tokenRedisService.saveRefreshToken(user.getId(), refreshToken);

            userRegisterResponse = modelMapper.map(user, UserRegisterResponse.class);
            userRegisterResponse.setRole(user.getRole().getCode());

        } catch (Exception e) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(userRegisterResponse)
                .build();
    }

    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader("Authorization");
        final String accessToken;
        final String userName;
        if (authHeader != null) {
            authHeader = authHeader.replaceAll("^\"|\"$", "");
            if (authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            } else
                throw new AppException(ErrorCode.TOKEN_INVALID);
        } else
            throw new AppException(ErrorCode.TOKEN_INVALID);
        userName = jwtService.extractUserName(accessToken);

        if (userName != null) {
            UserEntity user = userRepository.findById(userName).orElse(null);

            String refreshToken = tokenRedisService.getRefreshToken(userName);
            if (refreshToken == null) throw new AppException(ErrorCode.RE_TOKEN_EXPIRED);
            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            assert user != null;
            tokenRedisService.saveRefreshToken(user.getId(), newRefreshToken);


            UserRegisterResponse userRegisterResponse = modelMapper.map(user, UserRegisterResponse.class);
            userRegisterResponse.setRole(user.getRole().getCode());

            return AuthenticationResponse.builder()
                    .user(userRegisterResponse)
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
        }
        return null;
    }

    private void verifyToken(String token) {
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
                        user.getPassword()
                );

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
            if (status == OtpStatus.REGISTER){
                UserEntity user = userRepository.findByEmail(email).orElse(null);
                if (user == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);

                user.setStatus(true);
                userRepository.save(user);
            }
            return true;
        }
        return false;
    }

    public boolean sendOtp(String email) {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) throw new AppException(ErrorCode.USER_NOT_EXISTED);
        emailService.sendOtp(email);
        return true;
    }

}
