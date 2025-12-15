package com.java.chat_service.repository.feignClient;
import com.java.IntrospectRequest;
import com.java.IntrospectResponse;
import com.java.chat_service.config.security.AuthenticationRequestInterceptor;
import com.java.chat_service.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service",
        configuration = {AuthenticationRequestInterceptor.class})
public interface IdentityClient {
    @PostMapping("/auth/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request);
}