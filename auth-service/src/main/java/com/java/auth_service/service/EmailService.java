package com.java.auth_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface EmailService {
    Boolean sendEmail(MultipartFile[] file, String to, String cc, String subject, String body);

    String generateOtp();

    void sendOtp(String email);

    boolean checkOTP(String otp, String email);


}
