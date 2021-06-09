package com.github.peacetrue.sample.lock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 读写操作都用写锁，统计执行时间W；
 * 读操作用读锁，写操作用写锁，统计执行时间RW；
 * 执行时间W应该大于执行时间RW。
 *
 * @author : xiayx
 * @since : 2021-06-06 17:20
 **/
//tag::class-start[]
class CustomReadWriteLockTest {
    //end::class-start[]

    //tag::testReadWriteLock[]

    /** 重复测试次数 */
    public static final int REPEATED_COUNT = 100;
    /** 写线程数 */
    private int writeThreadCount = 1_00;
    /** 读线程数，读写比例：10/1 */
    private int readThreadCount = 1_000;
    /** 循环递增数 */
    private final int loopCount = 1_000;

    /** 并发读写 */
    private void concurrentReadWrite(Counter counter) throws InterruptedException {
        List<Thread> threads = buildThreads(counter);
        threads.forEach(Thread::start);
        for (Thread thread : threads) thread.join();
    }

    /** 构建线程 */
    private List<Thread> buildThreads(Counter counter) {
        return IntStream
                .range(0, writeThreadCount + readThreadCount)
                .mapToObj(buildThread(counter))
                .collect(Collectors.collectingAndThen(Collectors.toList(), threads -> {
                    //写线程在前，读线程在后，此操作乱序读写线程
                    Collections.shuffle(threads);
                    return threads;
                }));
    }

    private IntFunction<Thread> buildThread(Counter counter) {
        return i -> i < writeThreadCount
                ? new Thread(() -> counter.increase(loopCount))
                : new Thread(counter::getValue)
                ;
    }

    private long testReadWriteLock(Counter counter) throws Exception {
        long startTime = System.nanoTime();
        concurrentReadWrite(counter);
        int value = ((CounterAdapter) counter).getCounter().getValue();//绕开读锁适配
        Assertions.assertEquals(
                value, writeThreadCount * loopCount,
                "加锁后，多线程并发执行，计数值等于实际值"
        );
        return System.nanoTime() - startTime;
    }

    private void testReadWriteLock(CustomReadWriteLock readWriteLock, Supplier<Counter> supplier) throws Exception {
        CustomLock writeLock = readWriteLock.writeLock();
        CustomLock readLock = readWriteLock.readLock();
        //读操作是读锁写操作是写锁，读操作必须设置一个1毫秒的延迟
        long readWriteTimes = testReadWriteLock(new ReadWriteLockCounter(supplier.get(), writeLock, readLock));
        //读操作写操作都是写锁
        long writeTimes = testReadWriteLock(new ReadWriteLockCounter(supplier.get(), writeLock, writeLock));
        Assertions.assertTrue(readWriteTimes < writeTimes,
                String.format("读写锁耗时[%s]小于写写锁[%s]", readWriteTimes, writeTimes));
    }

    private void testReadWriteLock(CustomReadWriteLock readWriteLock) throws Exception {
        testReadWriteLock(readWriteLock, CounterImpl::new);
    }
    //end::testReadWriteLock[]

    //tag::reentrantReadWriteLock[]

    @RepeatedTest(REPEATED_COUNT)
    void reentrantReadWriteLock() throws Exception {
        //读取耗时短，读写锁和写写锁几乎没有区别
        testReadWriteLock(new ReadWriteLockAdapter(new ReentrantReadWriteLock()));
    }
    //end::reentrantReadWriteLock[]

    //tag::reentrantReadWriteLockInSlowCase[]

    @RepeatedTest(REPEATED_COUNT)
    void reentrantReadWriteLockInSlowCase() throws Exception {
        //读取耗时>=1ms时，读写锁耗时全部小于写写锁
        testReadWriteLock(new ReadWriteLockAdapter(new ReentrantReadWriteLock()), () -> new ReadWriteSlowCounter(new CounterImpl(), 1));
    }
    //end::reentrantReadWriteLockInSlowCase[]

    //tag::customReadWriteLockImpl[]

    @RepeatedTest(REPEATED_COUNT)
    void customReadWriteLockImpl() throws Exception {
        //读取耗时短，读写锁和写写锁几乎没有区别
        testReadWriteLock(new CustomReadWriteLockImpl());
    }
    //end::customReadWriteLockImpl[]

    //tag::customReadWriteLockImplInSlowCase[]

    @RepeatedTest(REPEATED_COUNT)
    void customReadWriteLockImplInSlowCase() throws Exception {
        //读取耗时>=1ms时，读写锁耗时全部小于写写锁
        testReadWriteLock(new CustomReadWriteLockImpl(), () -> new ReadWriteSlowCounter(new CounterImpl(), 1));
    }
    //end::customReadWriteLockImplInSlowCase[]

//tag::class-end[]
}
//end::class-end[]
