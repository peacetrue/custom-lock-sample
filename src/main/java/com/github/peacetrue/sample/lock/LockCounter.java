package com.github.peacetrue.sample.lock;

import lombok.Getter;

import java.util.Objects;

/**
 * @author : xiayx
 * @since : 2021-06-01 11:13
 **/
//tag::class[]
@Getter
public class LockCounter extends CounterAdapter {

    private final CustomLock customLock;

    public LockCounter(Counter counter, CustomLock customLock) {
        super(counter);
        this.customLock = Objects.requireNonNull(customLock);
    }

    @Override
    public void increase(int loopCount) {
        //写入时，施加写锁
        customLock.lock();
        super.increase(loopCount);
        customLock.unlock();
    }
}
//end::class[]
