package com.github.peacetrue.sample.lock;

/**
 * @author : xiayx
 * @since : 2021-05-31 20:43
 **/
//tag::class[]
public interface CustomLock {
    /** 加锁 */
    void lock();

    /** 解锁 */
    void unlock();
}
//end::class[]
