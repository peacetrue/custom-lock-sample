package com.github.peacetrue.sample.lock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 并发带来的问题
 *
 * @author : xiayx
 * @since : 2021-05-31 21:43
 **/
class CounterTest {

    private final int threadCount = 100, loopCount = 1_000_000;

    private void increase(Counter counter) throws InterruptedException {
        List<Thread> threads = IntStream
                .range(0, threadCount)
                .mapToObj(i -> new Thread(() -> counter.increase(loopCount)))
                .collect(Collectors.toList());
        threads.forEach(Thread::start);
        for (Thread thread : threads) thread.join();
    }

    @RepeatedTest(100)
    void concurrentProblem() throws Exception {
        Counter counter = new CounterImpl();
        increase(counter);
        Assertions.assertTrue(
                counter.getValue() < threadCount * loopCount,
                "多线程并发执行，数值小于实际值"
        );
    }

    private void testCustomLock(Counter counter, CustomLock customLock) throws Exception {
        Counter lockCounter = wrapLock(counter, customLock);
        increase(lockCounter);
        Assertions.assertEquals(counter.getValue(), threadCount * loopCount,
                "加锁后多线程并发累加结果等于实际值");
    }

    private Counter wrapLock(Counter counter, CustomLock customLock) {
        return new CounterAdapter(counter) {
            public void increase(int loopCount) {
                customLock.lock();
                super.increase(loopCount);
                customLock.unlock();
            }
        };
    }

    @RepeatedTest(100)
    void atomicLock() throws Exception {
        testCustomLock(new CounterImpl(), new AtomicLock());
    }

    @Test
    void atomicLockOfCpu() throws Exception {
        //测试 cpu 利用率高
        Counter counter = new CounterAdapter(new CounterImpl()) {
            @Override
            public void increase(int loopCount) {
                super.increase(loopCount);
                //等待 1 秒中，让其他线程疯狂抢锁
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        testCustomLock(counter, new AtomicLock());
    }
}
