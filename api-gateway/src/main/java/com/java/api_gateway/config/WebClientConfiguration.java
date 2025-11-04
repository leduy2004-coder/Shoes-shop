package com.java.api_gateway.config;

import com.java.api_gateway.repository.IdentityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@Slf4j
public class WebClientConfiguration {

    @Value("${app.web-client-url}")
    private String WEBCLIENT_URL;

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        log.info("✅ Creating LoadBalanced WebClient.Builder for {}", WEBCLIENT_URL);
        return WebClient.builder().baseUrl(WEBCLIENT_URL);
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        log.info("Building final WebClient...");
        return builder.build();
    }

    @Bean
    public IdentityClient identityClient(WebClient webClient) {
        log.info("Registering IdentityClient...");
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();
        return factory.createClient(IdentityClient.class);
    }
}
