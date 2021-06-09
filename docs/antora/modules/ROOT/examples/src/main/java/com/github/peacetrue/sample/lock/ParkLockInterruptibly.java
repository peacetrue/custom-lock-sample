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
public class ParkLockInterruptibly extends ReenterLock {

    @Override
    protected void handleWhenUnpark() {
        if (Thread.interrupted()) {
            throw new CustomInterruptedException();
        }
    }
}
//end::class[]
