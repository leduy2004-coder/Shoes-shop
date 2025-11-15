package com.java.auth_service.service.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class OtpRedisImpl extends BaseRedisServiceImpl<String,String,String> implements OtpRedisService {
    @Value("${expiration-OTP}")
    private long expiration;
    public OtpRedisImpl(RedisTemplate<String, String> redisTemplate, HashOperations<String, String, String> hashOperations) {
        super(redisTemplate, hashOperations);
    }

    private String getKeyFrom(String email) {
        return String.format("email:%s", email);
    }
    @Override
    public void clearByEmail(String email) {
        String key = this.getKeyFrom(email);
        super.delete(key);
    }

    @Override
    public String getOTP(String email) {
        String key = this.getKeyFrom(email);
        return super.get(key);
    }

    @Override
    public void saveOTP(String email, String OTP) {
        String key = this.getKeyFrom(email);
        super.set(key, OTP);
        super.setTimeToLive(key, expiration);
    }
}
