package com.github.peacetrue.sample.lock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * @author : xiayx
 * @since : 2021-06-08 22:06
 **/
//tag::class[]
@Slf4j
@Getter
public abstract class AbstractReadWriteLock implements CustomLock {

    public static final int STATE_NORMAL = 0;
    public static final int STATE_READ = 1;
    public static final int STATE_WRITE = 2;

    /** 状态：0 常规、1 读锁、2 写锁 */
    private final AtomicInteger state;
    /** 等锁队列（必须是基于 CAS 实现的线程安全队列，既然是自己实现锁，就不能使用 JDK 已经提供的锁） */
    private final Queue<Thread> waiters;
    /** 等锁时是否可打断，false 抛出异常 */
    private boolean interrupted;

    protected AbstractReadWriteLock(AtomicInteger state, Queue<Thread> waiters) {
        this(state, waiters, true);
    }

    protected AbstractReadWriteLock(AtomicInteger state, Queue<Thread> waiters, boolean interrupted) {
        this.state = Objects.requireNonNull(state);
        this.waiters = Objects.requireNonNull(waiters);
        this.interrupted = interrupted;
    }

    @Override
    public void lock() {
        Thread currentThread = Thread.currentThread();
        String currentThreadName = currentThread.getName();
        log.info("线程[{}]抢锁", currentThreadName);
        //已经抢到锁的场景下，重入锁
        LockOwner lockOwner = findLockOwner(currentThread);
        if (lockOwner != null) {
            log.debug("线程[{}]重入次数+1=[{}]", currentThreadName, lockOwner.increase());
            return;
        }

        //没抢到锁的场景下，抢锁
        if (tryAcquire()) return;

        while (true) {
            //抢不到锁，进入等锁队列（不重复入栈）
            if (!waiters.contains(currentThread)) {
                log.debug("线程[{}]进入等锁队列", currentThreadName);
                waiters.add(currentThread);
            }
            //队首线程抢锁
            if (waiters.peek() == currentThread && tryAcquire()) {
                log.debug("线程[{}]移出等锁队列", currentThreadName);
                waiters.poll();
                return;
            }
            LockSupport.park(currentThread);
            log.debug("线程[{}]被唤醒", currentThreadName);
            if (Thread.interrupted() && !interrupted) {
                throw new CustomInterruptedException();
            }
        }
    }

    @Nullable
    protected abstract LockOwner findLockOwner(Thread thread);

    protected boolean tryAcquire() {
        if (canLock()) {
            log.debug("线程[{}]抢到锁", Thread.currentThread().getName());
            setLockOwner();
            return true;
        }
        return false;
    }

    protected abstract boolean canLock();

    protected abstract void setLockOwner();

    @Override
    public void unlock() {
        Thread currentThread = Thread.currentThread();
        String currentThreadName = currentThread.getName();
        log.info("线程[{}]解锁", currentThreadName);
        LockOwner lockOwner = findLockOwner(currentThread);
        if (lockOwner != null) {
            doUnlock(lockOwner);
        } else {
            throw new IllegalMonitorStateException(String.format(
                    "线程[%s]尚未获得锁", currentThreadName
            ));
        }
    }

    protected void doUnlock(LockOwner lockOwner) {
        if (lockOwner.getReenterCount() > 0) {
            log.debug("线程[{}]重入次数-1=[{}]", Thread.currentThread().getName(), lockOwner.decrease());
            return;
        }

        clearLockOwner(lockOwner);
        setNormal();
        unparkHead();
    }

    protected abstract void clearLockOwner(LockOwner lockOwner);

    //没有线程持有锁，设置状态为：常规
    protected void setNormal() {
        state.set(STATE_NORMAL);
    }

    protected void unparkHead() {
        //唤醒头部等锁线程
        Thread headWaiter = waiters.peek();
        if (headWaiter == null) {
            log.debug("等锁队列尚无线程");
        } else {
            log.debug("唤醒队首等锁线程[{}]", headWaiter.getName());
            LockSupport.unpark(headWaiter);
        }
    }
}
//end::class[]
