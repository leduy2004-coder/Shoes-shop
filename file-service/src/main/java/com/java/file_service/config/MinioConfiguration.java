package com.java.file_service.config;

import com.java.file_service.config.properties.MinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfiguration {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())  // ← SỬA LẠI ĐÂY!
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}