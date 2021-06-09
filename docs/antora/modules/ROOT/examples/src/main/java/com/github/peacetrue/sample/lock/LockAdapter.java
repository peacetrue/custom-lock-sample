package com.github.peacetrue.sample.lock;

import java.util.Objects;
import java.util.concurrent.locks.Lock;

/**
 * @author : xiayx
 * @since : 2021-06-01 19:28
 **/
//tag::class[]
public class LockAdapter implements CustomLock {

    private final Lock lock;

    public LockAdapter(Lock lock) {
        this.lock = Objects.requireNonNull(lock);
    }

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }
}
//end::class[]
