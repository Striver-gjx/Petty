package com.petty.common.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JVM-level lock fallback when Redis is not available.
 * Provides single-instance concurrency protection only.
 */
@Slf4j
@Component
public class LocalDistributedLock implements DistributedLock {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(String key, long timeoutMs) {
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        boolean acquired = lock.tryLock();
        if (acquired) {
            log.debug("Acquired local lock: {}", key);
        }
        return acquired;
    }

    @Override
    public void unlock(String key) {
        ReentrantLock lock = locks.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("Released local lock: {}", key);
        }
    }
}
