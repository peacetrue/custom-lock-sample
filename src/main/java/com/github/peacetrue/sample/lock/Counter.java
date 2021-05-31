package com.github.peacetrue.sample.lock;

/**
 * @author : xiayx
 * @since : 2021-06-01 05:34
 **/
public interface Counter {
    void increase(int loopCount);

    int getValue();
}
