package com.github.peacetrue.sample.lock;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * 设置一个是否加锁的标志位，标志位为 false 时，表示未加锁；
 * 未加锁时可以加锁，已加锁则进入等锁队列，等待锁持有线程唤醒后继续抢锁。
 *
 * @author : xiayx
 * @since : 2021-05-31 20:20
 **/
//tag::class[]
@Slf4j
@Getter
@Setter
public class ParkLock implements CustomLock {

    /** 锁标志位 */
    private final AtomicBoolean locked = new AtomicBoolean(false);
    /** 等锁队列（必须是基于 CAS 实现的线程安全队列，既然是自己实现锁，就不能使用 JDK 已经提供的锁） */
    private final ConcurrentLinkedQueue<Thread> waiters = new ConcurrentLinkedQueue<>();
    /** 锁的拥有线程 */
    private Thread lockOwner;

    @Override
    public void lock() {
        Thread currentThread = Thread.currentThread();
        String currentThreadName = currentThread.getName();
        log.info("线程[{}]抢锁", currentThreadName);
        //如果能将 locked 从 false 设置为 true，则表示获得锁
        if (locked.compareAndSet(false, true)) {
            log.debug("线程[{}]取得锁", currentThreadName);
            lockOwner = currentThread;
            handleWhenLockSucceed();
            handleWhenLockCompleted();
        } else {
            if (intoWaiters()) {
                log.debug("线程[{}]进入等锁队列", currentThreadName);
                waiters.offer(currentThread);
            }
            handleWhenLockFailed();
            handleWhenLockCompleted();
            log.debug("挂起线程[{}]等锁", currentThreadName);
            LockSupport.park(this);
            log.debug("线程[{}]被唤醒", currentThreadName);
            handleWhenUnpark();
            lock();//锁释放后，再次抢锁
        }
    }

    /** 当抢锁成功后执行 */
    protected void handleWhenLockSucceed() {
    }

    /** 当抢锁失败后执行 */
    protected void handleWhenLockFailed() {
    }

    /** 当抢锁完成后执行 */
    protected void handleWhenLockCompleted() {
    }

    /** 当被唤醒后执行 */
    protected void handleWhenUnpark() {
    }

    /** 是否进入等锁队列 */
    protected boolean intoWaiters() {
        return true;
    }

    @Override
    public void unlock() {
        String currentThreadName = Thread.currentThread().getName();
        log.info("线程[{}]释放锁", currentThreadName);
        if (isOwnLock()) {
            doUnlock();
        } else {
            throw new IllegalStateException(String.format(
                    "线程[%s]尚未获得锁", currentThreadName
            ));
        }
    }

    /** 当前线程是否拥有锁 */
    protected boolean isOwnLock() {
        return lockOwner == Thread.currentThread();
    }

    /** 执行解锁 */
    protected void doUnlock() {
        locked.set(false);
        //期间可能插入新来的线程抢到锁，非公平锁
        unpackHead();
        handleWhenUnlockCompleted();
    }

    /** 唤醒头部线程 */
    protected void unpackHead() {
        Thread head = waiters.poll();
        if (head == null) {
            log.debug("尚无等锁线程");
        } else {
            log.debug("唤醒等锁线程[{}]", head.getName());
            LockSupport.unpark(head);
        }
    }

    /** 当解锁完成后执行 */
    protected void handleWhenUnlockCompleted() {
    }

}
//end::class[]
