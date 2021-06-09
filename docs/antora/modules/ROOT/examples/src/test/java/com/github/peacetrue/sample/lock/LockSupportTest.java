package com.github.peacetrue.sample.lock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static com.github.peacetrue.sample.lock.CounterTest.REPEATED_COUNT;

/**
 * @author : xiayx
 * @since : 2021-06-01 17:27
 **/
//tag::class[]
class LockSupportTest {

    private void interrupt(Runnable runnable) throws InterruptedException {
        Thread thread = new Thread(runnable);
        thread.start();
        thread.interrupt();
        thread.join();
    }

    @RepeatedTest(REPEATED_COUNT)
    void interruptPark() throws Exception {
        //打断一个 park 的线程后，唤醒该线程，继续执行
        boolean[] flag = {false};
        interrupt(() -> {
            LockSupport.park(this);
            flag[0] = true;
        });
        Assertions.assertTrue(flag[0]);
    }

    @RepeatedTest(REPEATED_COUNT)
    void parkAfterInterrupt() throws Exception {
        //打断一个 park 的线程后，未清除 interrupted 标记，后续 park 无法阻塞
        boolean[] flag = {false};
        interrupt(() -> {
            LockSupport.park(this);
            Assertions.assertTrue(Thread.currentThread().isInterrupted());//不清除打断标记
            flag[0] = true;

            long start = System.nanoTime();
            long waitNanos = TimeUnit.MILLISECONDS.toNanos(10);
            LockSupport.parkNanos(this, waitNanos);//此 park 无法阻塞
            Assertions.assertTrue(System.nanoTime() - start < waitNanos,
                    "parkNanos 没有阻塞代码，执行时间小于等待时间");
        });
        Assertions.assertTrue(flag[0]);
    }

    @RepeatedTest(100)
    void clearInterruptedAfterInterrupt() throws Exception {
        //打断一个 park 的线程后，清除 interrupted 标记，使 park 再度生效
        boolean[] flag = {false};
        interrupt(() -> {
            LockSupport.park(this);
            Assertions.assertTrue(Thread.interrupted());//清除打断标记
            flag[0] = true;

            long start = System.nanoTime();
            long waitNanos = TimeUnit.MILLISECONDS.toNanos(10);
            LockSupport.parkNanos(this, waitNanos);//此 park 重新阻塞
            Assertions.assertTrue(System.nanoTime() - start >= waitNanos,
                    "parkNanos 阻塞代码，执行时间大于等于等待时间");
        });
        Assertions.assertTrue(flag[0]);
    }
}
//end::class[]
