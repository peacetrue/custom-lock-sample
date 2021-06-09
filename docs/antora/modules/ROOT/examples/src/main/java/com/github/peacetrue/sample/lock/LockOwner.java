package com.github.peacetrue.sample.lock;

import lombok.Getter;

import java.util.Objects;

/**
 * @author : xiayx
 * @since : 2021-06-08 21:51
 **/
@Getter
public class LockOwner {

    private final Thread thread;
    private int reenterCount = 0;

    public LockOwner(Thread thread) {
        this.thread = Objects.requireNonNull(thread);
    }

    public int increase() {
        return ++reenterCount;
    }

    public int decrease() {
        return --reenterCount;
    }
}
