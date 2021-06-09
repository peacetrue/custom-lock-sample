package com.github.peacetrue.sample.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : xiayx
 * @since : 2021-06-08 22:06
 **/
//tag::class[]
@Slf4j
public class ReadLock extends AbstractReadWriteLock {

    /** 持有锁的线程集合 */
    private final ConcurrentLinkedQueue<LockOwner> owners = new ConcurrentLinkedQueue<>();

    public ReadLock(AtomicInteger state, Queue<Thread> waiters) {
        super(state, waiters);
    }

    public ReadLock(AtomicInteger state, Queue<Thread> waiters, boolean interrupted) {
        super(state, waiters, interrupted);
    }


    protected LockOwner findLockOwner(Thread thread) {
        return owners.stream().filter(item -> item.getThread() == thread).findAny().orElse(null);
    }

    protected boolean canLock() {
        return getState().get() == STATE_READ || getState().compareAndSet(STATE_NORMAL, STATE_READ);
    }

    protected void setLockOwner() {
        owners.add(new LockOwner(Thread.currentThread()));
    }

    protected void clearLockOwner(LockOwner lockOwner) {
        owners.remove(lockOwner);
    }

    protected void setNormal() {
        //没有线程持有读锁，设置状态为：常规
        if (owners.isEmpty() && getState().get() == STATE_READ) {
            getState().compareAndSet(STATE_READ, STATE_NORMAL);
        }
    }
}
//end::class[]
