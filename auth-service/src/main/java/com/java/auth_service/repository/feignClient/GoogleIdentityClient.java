package com.java.auth_service.repository.feignClient;

import com.java.auth_service.config.FeignFormEncoderConfig;
import com.java.auth_service.dto.response.ExchangeTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "google-identity", url = "https://oauth2.googleapis.com", configuration = FeignFormEncoderConfig.class)
public interface GoogleIdentityClient {

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenResponse.ExchangeTokenGoogle exchangeToken(@RequestBody Map<String, String> request);
}