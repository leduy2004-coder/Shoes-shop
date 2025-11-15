package com.java.api_gateway.config;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.api_gateway.dto.ApiResponse;
import com.java.api_gateway.service.IdentityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {
    IdentityService identityService;
    ObjectMapper objectMapper;

    @NonFinal
    private final String[] publicEndpoints = {
            "/auth/login",
            "/auth/register",
            "/auth/introspect",
            "/auth/refresh",
            "/auth/email/.*",
            "/auth/change-pass",

            "/shoes/products/get-by-id/.*",
            "/shoes/products/search",
            "/shoes/products/top-rated",
            "/shoes/products/get-all",
            "/shoes/banners/search",
            "/shoes/banners/get-by-slot/.*",
            "/shoes/reviews/get-by-product",
            "/shoes/categories/.*",
            "/shoes/brands/detail/.*",
            "/shoes/brands/search",
    };

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter authentication filter....");

        if (isPublicEndpoint(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        // Lấy trực tiếp từ HttpHeaders (có getFirst)
        String auth = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (auth == null || auth.isBlank() || !auth.startsWith("Bearer ")) {
            return unauthenticated(exchange.getResponse());
        }

        String token = auth.substring("Bearer ".length()).trim();
        log.info("Token: {}", token);

        return identityService.introspect(token)
                .doOnError(e -> log.error("Introspect error:", e))
                .flatMap(response -> response.getResult().isValid()
                        ? chain.filter(exchange)
                        : unauthenticated(exchange.getResponse()))
                .onErrorResume(e -> unauthenticated(exchange.getResponse()));
    }


    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request){
        return Arrays.stream(publicEndpoints)
                .anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }

    Mono<Void> unauthenticated(ServerHttpResponse response){
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1401)
                .message("Unauthenticated")
                .build();

        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}