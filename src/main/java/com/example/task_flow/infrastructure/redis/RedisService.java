package com.example.task_flow.infrastructure.redis;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service 
public class RedisService{
	public final StringRedisTemplate redisTemplate;
	private final Duration refreshTokenDuration = Duration.ofDays(7);

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

	public RedisService(StringRedisTemplate redisTemplate){
		this.redisTemplate = redisTemplate;
	}

	public void saveRefreshToken(String refreshToken,String userId) {
        try {
        	redisTemplate.opsForValue().set(refreshToken,userId, refreshTokenDuration);
        } catch (RedisConnectionFailureException e) {
        	log.error("Не удалось сохранить refresh token в Redis для пользователя: {}",userId);
        	throw new RuntimeException("Сервис аутентификации временно недоступен", e);
        }
      
    }

    public Long getUserIdRefreshToken(String refreshToken) {
        try {
        	String userId = redisTemplate.opsForValue().get(refreshToken);
        	return Long.valueOf(userId);

        } catch (RedisConnectionFailureException e) {
        	log.warn("Не удалось получить refresh token из Redis ",e);
            throw new RuntimeException("Сервис аутентификации временно недоступен", e);
        }
    }

    public void deleteRefreshToken(String refreshToken) {
        try {
        	redisTemplate.delete(refreshToken);
        } catch (RedisConnectionFailureException e) {
          log.warn("Не удалось удалить  из Redis refreshToken: {}");
        }    
    }

    public boolean hasRefreshToken(String refreshToken) {
        try {
        	return Boolean.TRUE.equals(redisTemplate.hasKey(refreshToken));
        } catch (RedisConnectionFailureException e) {
        log.warn("Не удалось найти refresh token в Redis ", e);
        return false;
        }   
      
    }
  public boolean isExpiringRefresh(String refreshToken, long days) {
    Long ttlSeconds = redisTemplate.getExpire(refreshToken, TimeUnit.SECONDS);

  
    if (ttlSeconds == null || ttlSeconds <= 0) {
        return true; 
    }

    long thresholdSeconds = TimeUnit.DAYS.toSeconds(days);
    return ttlSeconds <= thresholdSeconds;
}

}