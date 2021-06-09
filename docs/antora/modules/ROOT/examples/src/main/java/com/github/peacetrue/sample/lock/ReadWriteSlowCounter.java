package com.github.peacetrue.sample.lock;

/**
 * @author : xiayx
 * @since : 2021-06-06 18:55
 **/
//tag::class[]
public class ReadWriteSlowCounter extends SlowCounter {

    /** 读取延迟 */
    private final long readMillis;

    public ReadWriteSlowCounter(Counter counter, long readMillis) {
        this(counter, -1, readMillis);
    }

    public ReadWriteSlowCounter(Counter counter, long writeMillis, long readMillis) {
        super(counter, writeMillis);
        this.readMillis = readMillis;
    }

    @Override
    public int getValue() {
        sleep(readMillis);
        return super.getValue();
    }
}
//end::class[]
