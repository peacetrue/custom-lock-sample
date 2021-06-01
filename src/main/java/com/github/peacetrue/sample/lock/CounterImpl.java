package com.github.peacetrue.sample.lock;

import lombok.Getter;

import java.util.stream.IntStream;

/**
 * 计数器
 *
 * @author : xiayx
 * @since : 2021-05-31 21:55
 **/
//tag::class[]
@Getter
public class CounterImpl implements Counter {

    /** 计数值 */
    private int value = 0;

    /** 循环递增 */
    public void increase(int loopCount) {
        IntStream.range(0, loopCount)
                .forEach(ignored -> value++);
    }
}
//end::class[]
