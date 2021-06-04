package com.github.peacetrue.sample.lock;

import lombok.AllArgsConstructor;

import java.util.concurrent.locks.Lock;

/**
 * @author : xiayx
 * @since : 2021-06-01 19:28
 **/
@AllArgsConstructor
public class LockAdapter implements CustomLock {

    private final Lock lock;

    @Override
    public void lock() {
        lock.lock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }
}
