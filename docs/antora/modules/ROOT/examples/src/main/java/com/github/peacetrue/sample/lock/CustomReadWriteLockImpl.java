package com.github.peacetrue.sample.lock;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : xiayx
 * @since : 2021-06-07 19:41
 **/
public class CustomReadWriteLockImpl implements CustomReadWriteLock {

    /** 状态：0 常规、1 读锁、2 写锁 */
    private final AtomicInteger state = new AtomicInteger(0);
    /** 等锁队列（必须是基于 CAS 实现的线程安全队列，既然是自己实现锁，就不能使用 JDK 已经提供的锁） */
    private final Queue<Thread> waiters = new ConcurrentLinkedQueue<>();
    private final CustomLock readLock = new ReadLock(state, waiters);
    private final CustomLock writeLock = new WriteLock(state, waiters);

    @Override
    public CustomLock readLock() {
        return readLock;
    }

    @Override
    public CustomLock writeLock() {
        return writeLock;
    }
}
