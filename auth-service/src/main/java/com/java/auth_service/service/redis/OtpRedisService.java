package com.java.auth_service.service.redis;

public interface OtpRedisService extends BaseRedisService<String,String,String>{
    void clearByEmail(String email);
    String getOTP(String email);
    void saveOTP(String email, String OTP);
}
