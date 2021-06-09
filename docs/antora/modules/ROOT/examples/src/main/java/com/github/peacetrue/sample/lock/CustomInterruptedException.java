package com.github.peacetrue.sample.lock;

/**
 * 自定义的打断异常
 *
 * @author : xiayx
 * @see InterruptedException
 * @since : 2021-06-06 15:53
 **/
//tag::class
public class CustomInterruptedException extends RuntimeException {

    public CustomInterruptedException() {
        super();
    }

    public CustomInterruptedException(String message) {
        super(message);
    }
}
//end::class
