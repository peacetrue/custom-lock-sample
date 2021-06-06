package com.github.peacetrue.sample.lock;

/**
 * @author : xiayx
 * @since : 2021-05-31 20:43
 **/
//tag::class-start[]
public interface CustomLock {
    //end::class-start[]

    //tag::lock[]

    /** 加锁 */
    void lock();
    //end::lock[]

    //tag::unlock[]

    /** 解锁 */
    void unlock();
    //end::unlock[]

    //tag::class-end[]
}
//end::class-end[]
