package com.github.peacetrue.sample.lock;

/**
 * @author : xiayx
 * @since : 2021-06-06 17:16
 **/
//tag::class[]
public interface CustomReadWriteLock {
    /** 获取读锁 */
    CustomLock readLock();

    /** 获取写锁 */
    CustomLock writeLock();
}
//end::class[]
