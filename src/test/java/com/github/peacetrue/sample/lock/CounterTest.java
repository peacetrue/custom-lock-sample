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
//tag::class-start[]
class CounterTest {

    //end::class-start[]

    //tag::concurrentProblem[]
    private final int threadCount = 100, loopCount = 1_000_000;

    @RepeatedTest(100)
    void concurrentProblem() throws Exception {
        Counter counter = new CounterImpl();
        increase(counter);
        Assertions.assertTrue(
                counter.getValue() < threadCount * loopCount,
                "多线程并发执行，计数值小于实际值"
        );
    }

    private void increase(Counter counter) throws InterruptedException {
        List<Thread> threads = IntStream
                .range(0, threadCount)
                .mapToObj(i -> new Thread(() -> counter.increase(loopCount)))
                .collect(Collectors.toList());
        threads.forEach(Thread::start);
        for (Thread thread : threads) thread.join();
    }
    //end::concurrentProblem[]

    //tag::testCustomLock[]
    private void testCustomLock(CustomLock customLock) throws Exception {
        testCustomLock(new LockCounter(new CounterImpl(), customLock));
    }

    private void testCustomLock(Counter counter) throws Exception {
        increase(counter);
        Assertions.assertEquals(
                counter.getValue(), threadCount * loopCount,
                "加锁后，多线程并发执行，计数值等于实际值"
        );
    }
    //end::testCustomLock[]


    //tag::atomicLock[]
    @RepeatedTest(100)
    void atomicLock() throws Exception {
        testCustomLock(new AtomicLock());
    }
    //end::atomicLock[]

    //tag::atomicLockInSlowCase[]
    @Test
    void atomicLockInSlowCase() throws Exception {
        //慢场景下执行，CPU 利用率高
        testCustomLock(new LockCounter(new SlowCounter(new CounterImpl()), new AtomicLock()));
    }
    //end::atomicLockInSlowCase[]

    //tag::parkLock[]
    @RepeatedTest(100)
    void parkLock() throws Exception {
        testCustomLock(new ParkLock());
    }
    //end::parkLock[]

    //tag::parkLockInSlowCase[]
    @Test
    void parkLockInSlowCase() throws Exception {
        //慢场景下执行
        testCustomLock(new LockCounter(new SlowCounter(new CounterImpl()), new ParkLock()));
    }
    //end::parkLockInSlowCase[]

//tag::class-end[]
}
//end::class-end[]
