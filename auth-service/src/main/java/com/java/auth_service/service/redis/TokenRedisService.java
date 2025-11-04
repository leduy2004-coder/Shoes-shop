package com.java.auth_service.service.redis;

public interface TokenRedisService extends BaseRedisService<String,String,String>{
    void clearByUserName(String userName);
    String getRefreshToken(String userName);
    void saveRefreshToken(String userName, String refreshToken);
}
