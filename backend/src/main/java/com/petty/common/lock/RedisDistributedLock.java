package com.petty.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true")
@RequiredArgsConstructor
public class RedisDistributedLock implements DistributedLock {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "petty:lock:";

    @Override
    public boolean tryLock(String key, long timeoutMs) {
        String lockKey = LOCK_PREFIX + key;
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, Thread.currentThread().getName(), Duration.ofMillis(timeoutMs));
        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Acquired distributed lock: {}", lockKey);
            return true;
        }
        log.debug("Failed to acquire distributed lock: {}", lockKey);
        return false;
    }

    @Override
    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        redisTemplate.delete(lockKey);
        log.debug("Released distributed lock: {}", lockKey);
    }
}
