package likelion.team1.mindscape.service;

import likelion.team1.mindscape.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisRefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = JwtProperties.REFRESH_TOKEN_STRING + ":";

    public void saveRefreshToken(String accountId, String refreshToken, long expirationTime) {
        String key = REFRESH_TOKEN_PREFIX + accountId;
        redisTemplate.opsForValue().set(key, refreshToken, expirationTime, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String accountId) {
        String key = REFRESH_TOKEN_PREFIX + accountId;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteRefreshToken(String accountId) {
        String key = REFRESH_TOKEN_PREFIX + accountId;
        redisTemplate.delete(key);
    }

    public boolean validateRefreshToken(String accountId, String refreshToken) {
        String savedToken = getRefreshToken(accountId);
        return refreshToken.equals(savedToken);
    }
}
