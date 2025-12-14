package com.java.auth_service.config;

import feign.codec.Encoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.FormHttpMessageConverter;

public class FeignFormEncoderConfig {

    @Bean
    public Encoder feignFormEncoder() {
        FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(formConverter);
        return new SpringEncoder(objectFactory);
    }
}

