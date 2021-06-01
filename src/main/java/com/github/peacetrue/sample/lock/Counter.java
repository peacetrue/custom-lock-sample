package com.github.peacetrue.sample.lock;

/**
 * 计数器
 *
 * @author : xiayx
 * @since : 2021-06-01 05:34
 **/
//tag::class[]
public interface Counter {
    /** 循环递增 */
    void increase(int loopCount);

    /** 获取计数值 */
    int getValue();
}
//end::class[]
