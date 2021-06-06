package com.github.peacetrue.sample.lock;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 计数器测试
 *
 * @author : xiayx
 * @since : 2021-05-31 21:43
 **/
//tag::class-start[]
@Slf4j
@Timeout(1)
class CounterTest {

    //end::class-start[]

    //tag::concurrentProblem[]
    /** 重复测试数，如果 1 万次还没测出问题，就姑且认为正确吧 */
    public static final int REPEATED_COUNT = 10_000;
    /** 线程数 */
    private int threadCount = 1_00;
    /** 循环递增数 */
    private final int loopCount = 1_000;

    @RepeatedTest(REPEATED_COUNT)
    void concurrentProblem() throws Exception {
        //此测试结果无法通过
        Counter counter = new CounterImpl();
        concurrentIncrease(counter);
        Assertions.assertEquals(counter.getValue(), threadCount * loopCount,
                "多线程并发执行，计数值等于实际值");
    }

    /** 并发递增 */
    private void concurrentIncrease(Counter counter) throws InterruptedException {
        List<Thread> threads = buildThreads(counter);
        threads.forEach(Thread::start);
        for (Thread thread : threads) thread.join();
    }

    /** 构建线程 */
    private List<Thread> buildThreads(Counter counter) {
        return IntStream
                .range(0, threadCount)
                .mapToObj(i -> new Thread(() -> counter.increase(loopCount)))
                .collect(Collectors.toList());
    }
    //end::concurrentProblem[]

    //tag::testCustomLock[]
    private void testCustomLock(CustomLock customLock) throws Exception {
        testCustomLock(new LockCounter(new CounterImpl(), customLock));
    }

    private void testCustomLock(Counter counter) throws Exception {
        concurrentIncrease(counter);
        Assertions.assertEquals(
                counter.getValue(), threadCount * loopCount,
                "加锁后，多线程并发执行，计数值等于实际值"
        );
    }
    //end::testCustomLock[]

    //tag::reentrantLock[]
    @RepeatedTest(REPEATED_COUNT)
    void reentrantLock() throws Exception {
        //JDK 的锁与自定义的锁，做个对比
        testCustomLock(new LockAdapter(new ReentrantLock()));
    }
    //end::reentrantLock[]

    //tag::atomicLock[]
    @RepeatedTest(REPEATED_COUNT)
    void atomicLock() throws Exception {
        testCustomLock(new AtomicLock());
    }
    //end::atomicLock[]

    //tag::atomicLockInSlowCase[]
    @Test
    @Timeout(10)
    void atomicLockInSlowCase() throws Exception {
        //此测试过程中会导致 CPU 利用率达到 100%
        testCustomLock(new LockCounter(new SlowCounter(new CounterImpl(), 20), new AtomicLock()));
    }
    //end::atomicLockInSlowCase[]

    //tag::parkLock[]
    @RepeatedTest(REPEATED_COUNT)
    void parkLock() throws Exception {
        //此测试结果无法通过，因为抢锁和解锁不互斥
        testCustomLock(new ParkLock());
    }
    //end::parkLock[]

    //tag::parkLockMutex[]
    @RepeatedTest(REPEATED_COUNT)
    void parkLockMutex() throws Exception {
        //此测试结果无法通过，因为 LockSupport.park() 可能阻塞不住
        testCustomLock(new ParkLockMutex());
    }
    //end::parkLockMutex[]

    //tag::parkLockMutexBlockFailed[]
    @RepeatedTest(REPEATED_COUNT)
    void parkLockMutexBlockFailed() throws Exception {
        //此测试结果正常通过
        testCustomLock(new ParkLockMutexBlockFailed());
    }
    //end::parkLockMutexBlockFailed[]

    //tag::parkLockMutexBlockFailedInSlowCase[]
    @Test
    @Timeout(100)
    void parkLockMutexBlockFailedInSlowCase() throws Exception {
        //此测试结果正常通过
        testCustomLock(new LockCounter(new SlowCounter(new CounterImpl(), 100), new ParkLockMutexBlockFailed()));
    }
    //end::parkLockMutexBlockFailedInSlowCase[]

    //tag::parkLockNotMutex[]
    @Timeout(1)
    @RepeatedTest(REPEATED_COUNT)
    void parkLockNotMutex() throws Exception {
        //此测试结果正常通过
        testCustomLock(new ParkLockNotMutex());
    }
    //end::parkLockNotMutex[]

    //tag::parkLockNotMutexInSlowCase[]
    @Timeout(100)
    @Test
    void parkLockNotMutexInSlowCase() throws Exception {
        testCustomLock(new LockCounter(new SlowCounter(new CounterImpl(), 100), new ParkLockNotMutex()));
    }
    //end::parkLockNotMutexInSlowCase[]

    //tag::parkLockInReenterCase[]
    @RepeatedTest(1)
    void parkLockInReenterCase() throws Exception {
        //挂起锁在重入场景下的测试
        ParkLock parkLock = new ParkLockMutexBlockFailed();
        //嵌套加锁形成锁重入
        LockCounter counter = new LockCounter(new LockCounter(new CounterImpl(), parkLock), parkLock);
        //第 1 重加锁获得锁，第 2 重加锁进入等锁队列，形成死锁
        new Thread(() -> counter.increase(loopCount)).start();
        //阻塞到线程已进入等锁队列
        while (true) if (!parkLock.getWaiters().isEmpty()) break;
        Assertions.assertTrue(
                parkLock.getWaiters().contains(parkLock.getLockOwner()),
                "锁的拥有线程同时进入到了等锁队列形成死锁"
        );
    }
    //end::parkLockInReenterCase[]

    //tag::reenterLock[]
    @RepeatedTest(REPEATED_COUNT)
    void reenterLock() throws Exception {
        ReenterLock reenterLock = new ReenterLock();
        testCustomLock(new LockCounter(new LockCounter(new CounterImpl(), reenterLock), reenterLock));
    }
    //end::reenterLock[]

    //tag::interruptWaiter[]
    private void interruptWaiter(ParkLock parkLock) throws Exception {
        LockCounter lockCounter = new LockCounter(new SlowCounter(new CounterImpl(), 3_000), parkLock);

        Thread thread1 = new Thread(() -> lockCounter.increase(loopCount));
        log.info("thread1.start");
        thread1.start();
        //阻塞到线程取得锁
        while (true) if (parkLock.getLockOwner() == thread1) break;

        //启动一个线程，进入到等锁队列
        Thread thread2 = new Thread(() -> lockCounter.increase(loopCount));
        log.info("thread2.start");
        thread2.start();
        //thread1 需要执行 3 秒，thread2 抢不到锁，会进入等锁队列
        while (true) {
            if (parkLock.getWaiters().contains(thread2)) break;
            if (parkLock.getLockOwner() == thread2) throw new IllegalStateException();
        }

        //打断等锁线程，看看会发生什么
        log.info("thread2.interrupt");
        thread2.interrupt();//打断之后，依然抢不到锁
        thread1.join();
        thread2.join();
    }
    //end::interruptWaiter[]

    //tag::parkLockInInterruptWaiterCase[]
    @RepeatedTest(1)
    @Timeout(4)
    void parkLockInInterruptWaiterCase() throws Exception {
        interruptWaiter(new ReenterLock());
    }
    //end::parkLockInInterruptWaiterCase[]

    //tag::parkLockClearInterrupted[]
    @Test
    @Timeout(7)
    void parkLockClearInterrupted() throws Exception {
        interruptWaiter(new ParkLockClearInterrupted());
    }
    //end::parkLockClearInterrupted[]

    //tag::parkLockInterruptibly[]
    @Test
    @Timeout(7)
    void parkLockInterruptibly() throws Exception {
        interruptWaiter(new ParkLockInterruptibly());
    }
    //end::parkLockInterruptibly[]

//tag::class-end[]
}
//end::class-end[]
