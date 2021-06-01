package com.github.peacetrue.sample.lock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;

/**
 * @author : xiayx
 * @since : 2021-06-01 19:24
 **/
class ReentrantLockTest {

    public static class ReentrantLock extends java.util.concurrent.locks.ReentrantLock {
        public ReentrantLock() {
        }

        public ReentrantLock(boolean fair) {
            super(fair);
        }

        @Override
        public Thread getOwner() {
            return super.getOwner();
        }

        @Override
        public Collection<Thread> getQueuedThreads() {
            return super.getQueuedThreads();
        }
    }

    @Test
    void interruptWaiter() {
        //打断一个等待的线程
        ReentrantLock reentrantLock = new ReentrantLock();
        LockAdapter customLock = new LockAdapter(reentrantLock);
        int loopCount = 10_000;
        LockCounter lockCounter = new LockCounter(new SlowCounter(new CounterImpl(), loopCount), customLock);
        Thread thread1 = new Thread(() -> lockCounter.increase(loopCount));
        thread1.start();
        //阻塞到线程获取锁
        while (reentrantLock.getOwner() == null) {
        }
        Assertions.assertEquals(thread1, reentrantLock.getOwner());

        //启动一个线程，进入到等锁队列
        Thread thread2 = new Thread(() -> lockCounter.increase(loopCount));
        thread2.start();
        //阻塞直到线程进入等锁队列
        while (!reentrantLock.hasQueuedThreads()) {
        }
        Assertions.assertEquals(thread2, reentrantLock.getQueuedThreads().iterator().next());
        thread2.interrupt();
//      Assertions.assertEquals(2, parkLock.getWaiters().size());
    }
}
