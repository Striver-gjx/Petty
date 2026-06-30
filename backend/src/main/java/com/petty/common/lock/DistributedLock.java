package com.petty.common.lock;

public interface DistributedLock {

    boolean tryLock(String key, long timeoutMs);

    void unlock(String key);
}
