package com.github.peacetrue.sample.lock;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @author : xiayx
 * @since : 2021-06-07 08:38
 **/
//tag::class[]
public class ReadWriteLockAdapter implements CustomReadWriteLock {

    private final ReadWriteLock readWriteLock;

    public ReadWriteLockAdapter(ReadWriteLock readWriteLock) {
        this.readWriteLock = Objects.requireNonNull(readWriteLock);
    }

    @Override
    public CustomLock readLock() {
        return new LockAdapter(readWriteLock.readLock());
    }

    @Override
    public CustomLock writeLock() {
        return new LockAdapter(readWriteLock.writeLock());
    }
}
//end::class[]
