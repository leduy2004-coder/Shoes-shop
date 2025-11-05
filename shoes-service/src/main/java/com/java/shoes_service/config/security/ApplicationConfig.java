package com.java.shoes_service.config.security;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class ApplicationConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            // Không xác thực chữ ký, chỉ parse token
            try {
                SignedJWT signedJWT = SignedJWT.parse(token);

                return new Jwt(
                        token,
                        signedJWT.getJWTClaimsSet().getIssueTime().toInstant(),
                        signedJWT.getJWTClaimsSet().getExpirationTime().toInstant(),
                        signedJWT.getHeader().toJSONObject(),
                        signedJWT.getJWTClaimsSet().getClaims()
                );
            } catch (Exception e) {
                throw new JwtException("Invalid JWT");
            }
        };
    }


//    @Bean
//    public AuditorAware<String> auditorAware() {
//        return new JpaAuditingConfig.AuditorAwareImpl();
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


}
