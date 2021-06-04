package com.github.peacetrue.sample.lock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 可重入锁
 *
 * @author : xiayx
 * @since : 2021-05-31 20:20
 **/
//tag::class[]
@Slf4j
@Getter
public class ReenterLock extends ParkLockMutexBlockFailed {

    /** 重入次数 */
    private int reenterCount = 0;

    @Override
    public void lock() {
        if (isOwnLock()) {
            //已持有锁，重入次数+1
            log.debug("线程[{}]重入次数+1=[{}]", Thread.currentThread().getName(), ++reenterCount);
            return;
        }
        super.lock();
    }

    @Override
    protected void doUnlock() {
        if (reenterCount > 0) {
            //已持有锁，重入次数-1
            log.debug("线程[{}]重入次数-1=[{}]", Thread.currentThread().getName(), --reenterCount);
        } else {
            super.doUnlock();
        }
    }
}
//end::class[]
