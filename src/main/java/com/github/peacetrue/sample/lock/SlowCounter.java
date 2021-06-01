package com.github.peacetrue.sample.lock;

/**
 * @author : xiayx
 * @since : 2021-06-01 09:03
 **/
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
        //等待 1 秒中，让其他线程疯狂抢锁
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
