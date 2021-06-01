package com.github.peacetrue.sample.lock;

/**
 * @author : xiayx
 * @since : 2021-06-01 09:03
 **/
//tag::class[]
public class SlowCounter extends CounterAdapter {

    private final long millis;

    public SlowCounter(Counter counter) {
        this(counter, 100);
    }

    public SlowCounter(Counter counter, long millis) {
        super(counter);
        this.millis = millis;
    }

    @Override
    public void increase(int loopCount) {
        super.increase(loopCount);
        try {
            //等待一段时间，让其他线程疯狂抢锁
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
//end::class[]
