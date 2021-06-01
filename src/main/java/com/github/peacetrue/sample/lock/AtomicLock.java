package com.github.peacetrue.sample.lock;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 设置一个是否加锁的标志位，标志位为 false 时，表示未加锁；
 * 未加锁时可以加锁，已加锁则需要等待锁释放；
 * 锁释放后可以立即抢锁。
 *
 * @author : xiayx
 * @since : 2021-05-31 20:20
 **/
//tag::class[]
public class AtomicLock implements CustomLock {

    private final AtomicBoolean locked = new AtomicBoolean(false);
    private Thread lockOwner;

    @Override
    public void lock() {
        //如果能将 locked 从 false 设置为 true，则表示获得锁；
        //否则不停地尝试获取锁
        while (!locked.compareAndSet(false, true)) {
        }
        //获取锁成功，锁的所有者设置为自己
        lockOwner = Thread.currentThread();
    }

    @Override
    public void unlock() {
        if (lockOwner == Thread.currentThread()) {
            locked.set(false);
        } else {
            throw new IllegalStateException(String.format(
                    "线程[%s]尚未获得锁", Thread.currentThread().getName()
            ));
        }
    }
}
//end::class[]
