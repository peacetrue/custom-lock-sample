package com.github.peacetrue.sample.lock;

import lombok.Getter;

/**
 * 计数器
 *
 * @author : xiayx
 * @since : 2021-05-31 21:55
 **/
@Getter
public class Counter {

    /** 计数值 */
    private int value = 0;

    /** 循环递增 */
    public void increase(int loopCount) {
        for (int i = 0; i < loopCount; i++) {
            value++;
        }
    }
}
