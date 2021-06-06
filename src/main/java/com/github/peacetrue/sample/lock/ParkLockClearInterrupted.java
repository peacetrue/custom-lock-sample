package com.github.peacetrue.sample.lock;

import lombok.extern.slf4j.Slf4j;

/**
 * 清除打断标志
 *
 * @author : xiayx
 * @since : 2021-06-06 10:29
 **/
//tag::class[]
@Slf4j
public class ParkLockClearInterrupted extends ReenterLock {
    @Override
    protected void handleWhenUnpark() {
        if (Thread.interrupted()) {
            log.debug("已清除线程[{}]的打断标记", Thread.currentThread().getName());
        }
    }
}
//end::class[]
