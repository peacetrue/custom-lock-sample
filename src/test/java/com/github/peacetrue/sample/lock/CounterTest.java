package com.github.peacetrue.sample.lock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 计数器测试
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

    //tag::parkLockInReenterCase[]
    @Test
    void parkLockInReenterCase() throws Exception {
        ParkLock parkLock = new ParkLock();
        //嵌套加锁形成重入锁
        LockCounter counter = new LockCounter(new LockCounter(new CounterImpl(), parkLock), parkLock);
        //第 1 重加锁获得锁，第 2 重加锁进入等待队列，形成死锁
        new Thread(() -> counter.increase(loopCount), "ReenterLockTestThread").start();
        Thread.sleep(1_000);
        Assertions.assertTrue(
                parkLock.getWaiters().contains(parkLock.getLockOwner()),
                "锁的拥有线程同时进入到了等待队列形成死锁"
        );
    }
    //end::parkLockInReenterCase[]

    //tag::reenterLock[]
    @RepeatedTest(100)
    void reenterLock() throws Exception {
        ReenterLock reenterLock = new ReenterLock();
        LockCounter counter = new LockCounter(new LockCounter(new CounterImpl(), reenterLock), reenterLock);
        testCustomLock(counter);
    }
    //end::reenterLock[]

//tag::class-end[]
}
//end::class-end[]
