package com.java.shoes_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ShoesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShoesServiceApplication.class, args);
	}

}
