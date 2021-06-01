package com.github.peacetrue.sample.lock;

import lombok.Getter;

/**
 * 可重入锁
 *
 * @author : xiayx
 * @since : 2021-05-31 20:20
 **/
//tag::class[]
@Getter
public class ReenterLock extends ParkLock {

    protected int reenterCount = 0;

    @Override
    public void lock() {
        if (isOwnLock()) {
            //已持有锁，重入次数+1
            reenterCount++;
            return;
        }
        super.lock();
    }

    @Override
    protected void release() {
        if (reenterCount > 0) {
            //已持有锁，重入次数-1
            reenterCount--;
        } else {
            super.release();
        }
    }
}
//end::class[]
