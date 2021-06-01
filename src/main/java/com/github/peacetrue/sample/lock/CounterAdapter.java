package com.github.peacetrue.sample.lock;

import lombok.AllArgsConstructor;

/**
 * @author : xiayx
 * @since : 2021-06-01 05:36
 **/
//tag::class[]
@AllArgsConstructor
public class CounterAdapter implements Counter {

    private final Counter counter;

    @Override
    public void increase(int loopCount) {
        counter.increase(loopCount);
    }

    @Override
    public int getValue() {
        return counter.getValue();
    }
}
//end::class[]
