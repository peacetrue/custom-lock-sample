package com.github.peacetrue.sample.lock;

import java.util.Objects;

/**
 * @author : xiayx
 * @since : 2021-06-06 17:44
 **/
//tag::class[]
public class ReadWriteLockCounter extends LockCounter {

    private final CustomLock readLock;

    public ReadWriteLockCounter(Counter counter, CustomLock writeLock, CustomLock readLock) {
        super(counter, writeLock);
        this.readLock = Objects.requireNonNull(readLock);
    }

    @Override
    public int getValue() {
        //读取时，施加读锁
        readLock.lock();
        int value = super.getValue();
        readLock.unlock();
        return value;
    }
}
//end::class[]
