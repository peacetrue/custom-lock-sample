package com.github.peacetrue.sample.lock;

import lombok.Getter;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * 设置一个是否加锁的标志位，标志位为 false 时，表示未加锁；
 * 未加锁时可以加锁，已加锁则进入等待队列，等待锁持有线程唤醒后继续抢锁。
 *
 * @author : xiayx
 * @since : 2021-05-31 20:20
 **/
//tag::class[]
@Getter
public class ParkLock implements CustomLock {

    /** 锁标志位 */
    private final AtomicBoolean locked = new AtomicBoolean(false);
    /** 等待线程队列（必须是基于 CAS 实现的线程安全队列，既然是自己实现锁，就不能使用 JDK 已经提供的锁） */
    private final ConcurrentLinkedQueue<Thread> waiters = new ConcurrentLinkedQueue<>();
    /** 锁的拥有线程 */
    private Thread lockOwner;

    @Override
    public void lock() {
        //如果能将 locked 从 false 设置为 true，则表示获得锁；
        if (locked.compareAndSet(false, true)) {
            lockOwner = Thread.currentThread();
        } else {
            //未获得锁，放入等待队列，并进行等待
            waiters.offer(Thread.currentThread());
            LockSupport.park(this);
            //锁释放后，再次抢锁
            lock();
        }
    }

    @Override
    public void unlock() {
        if (isOwnLock()) {
            release();
        } else {
            throw new IllegalStateException(String.format(
                    "线程[%s]尚未获得锁", Thread.currentThread().getName()
            ));
        }
    }

    protected boolean isOwnLock() {
        return lockOwner == Thread.currentThread();
    }

    protected void release() {
        locked.set(false);
        Thread head = waiters.poll();
        if (head != null) LockSupport.unpark(head);
    }

}
//end::class[]
