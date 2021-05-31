package com.github.peacetrue.sample.lock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

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
        Counter counter = new Counter();
        increase(counter);
        Assertions.assertTrue(
                counter.getValue() < threadCount * loopCount,
                "多线程并发执行，数值小于实际值"
        );
    }

}
