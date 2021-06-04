package com.github.peacetrue.sample.lock;

/**
 * 处理阻塞不住
 *
 * @author : xiayx
 * @since : 2021-06-04 06:56
 **/
//tag::class[]
public class ParkLockMutexBlockFailed extends ParkLockMutex {

    @Override
    protected void handleWhenLockSucceed() {
        //没阻塞住，然后抢锁成功，从等锁队列中移除
        getWaiters().remove(Thread.currentThread());
    }

    @Override
    protected boolean intoWaiters() {
        //没阻塞住，还在等锁队列中，不重复入栈
        return !getWaiters().contains(Thread.currentThread());
    }
}
//end::class[]
