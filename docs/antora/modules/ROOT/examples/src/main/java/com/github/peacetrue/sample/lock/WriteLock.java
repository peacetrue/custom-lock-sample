package com.github.peacetrue.sample.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : xiayx
 * @since : 2021-06-08 22:16
 **/
//tag::class[]
@Slf4j
public class WriteLock extends AbstractReadWriteLock {

    private LockOwner lockOwner;

    public WriteLock(AtomicInteger state, Queue<Thread> waiters) {
        super(state, waiters);
    }

    public WriteLock(AtomicInteger state, Queue<Thread> waiters, boolean interrupted) {
        super(state, waiters, interrupted);
    }

    @Override
    protected LockOwner findLockOwner(Thread thread) {
        if (lockOwner != null && lockOwner.getThread() == thread) return lockOwner;
        return null;
    }

    protected boolean canLock() {
        return getState().compareAndSet(STATE_NORMAL, STATE_WRITE);
    }

    protected void setLockOwner() {
        lockOwner = new LockOwner(Thread.currentThread());
    }

    protected void clearLockOwner(LockOwner ignored) {
        this.lockOwner = null;
    }

    protected void setNormal() {
        //没有线程持有读锁，设置状态为：常规
        boolean success = getState().compareAndSet(STATE_WRITE, STATE_NORMAL);
        if (!success) throw new IllegalStateException("从读取态更新为常规态异常");
    }
}
//end::class[]
