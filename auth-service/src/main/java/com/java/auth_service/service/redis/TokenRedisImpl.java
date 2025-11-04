package com.java.auth_service.service.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenRedisImpl extends BaseRedisServiceImpl<String,String,String> implements TokenRedisService {

    @Value("${spring.application.security.jwt.refresh-token.expiration}")
    private long expiration;

    public TokenRedisImpl(RedisTemplate<String, String> redisTemplate, HashOperations<String, String, String> hashOperations) {
        super(redisTemplate, hashOperations);
    }

    private String getKeyFrom(String userName) {
        return String.format("refresh_token:%s", userName);
    }

    @Override
    public String getRefreshToken(String userName) {
        String key = this.getKeyFrom(userName);
        return super.get(key);
    }

    @Override
    public void saveRefreshToken(String userName, String refreshToken) {
        String key = this.getKeyFrom(userName);
        super.set(key, refreshToken);
        super.setTimeToLive(key, expiration);
    }

    @Override
    public void clearByUserName(String userName) {
        String key = this.getKeyFrom(userName);
        super.delete(key);
    }
}
