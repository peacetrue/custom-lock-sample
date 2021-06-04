package com.github.peacetrue.sample.lock;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 加锁和加锁之间不互斥
 *
 * @author : xiayx
 * @since : 2021-06-03 11:50
 **/
//tag::class[]
@Slf4j
public class ParkLockNotMutex extends ParkLockMutexBlockFailed {

    /*
     * 状态变更逻辑
     * 场景             状态  抢锁中线程数 备注
     * 初始：           常规态 0
     * 第1个线程抢锁开始：抢锁中 1          状态变更，线程数为1
     * 第n个线程抢锁开始：抢锁中 n          状态不变，线程数递增
     * 第n个线程抢锁结束：抢锁中 n-1        状态不变，线程数递减
     * 第1个线程抢锁结束：常规态 0          线程数递减为0，状态变更
     * 第1个线程解锁开始：解锁中 0          状态变更，线程数不变
     * 第1个线程解锁结束：常规态 0          状态变更，线程数不变
     */
    /** 抢锁中线程数 */
    private final AtomicInteger lockingCount = new AtomicInteger(0);

    @Override
    protected boolean canLock() {
        while (true) {
            //如果是抢锁中，递增抢锁中线程数量；不是抢锁中，设置状态为抢锁中，并且初始化抢锁中线程数量
            if ((isLocking() && increaseLockingCount()) || (setLocking() && initLockingCount())) {
                return true;
            }
        }
    }

    @Override
    protected boolean isLocking() {
        //状态是抢锁中同时必须至少有一个抢锁中的线程
        return super.isLocking() && lockingCount.get() > 0;
    }

    /** 递增抢锁中线程数量 */
    protected boolean increaseLockingCount() {
        while (true) {
            int count = lockingCount.get();
            //如果在判断状态的同时，改变了抢锁中线程数，则此次状态判断失效
            if (!isLocking()) return false;
            if (lockingCount.compareAndSet(count, count + 1)) {
                log.debug("抢锁中线程数+1({})=[{}]", Thread.currentThread().getName(), count + 1);
                return true;
            }
        }
    }

    @Override
    protected boolean setLocking() {
        if (getState().compareAndSet(STATE_NORMAL, STATE_LOCKING)) {
            log.debug("线程[{}]设置状态为[抢锁中]", Thread.currentThread().getName());
            return true;
        }
        return false;
    }

    protected boolean initLockingCount() {
        //必须是从 0 到 1，否则状态异常
        if (!lockingCount.compareAndSet(0, 1)) {
            throw new IllegalStateException(
                    String.format("初始化抢锁中线程数异常(except=%s, actual=%s)", 0, lockingCount.get())
            );
        }
        log.debug("初始化抢锁中线程数({})=[1]", Thread.currentThread().getName());
        return true;
    }

    @Override
    protected void handleWhenLockCompleted() {
        int count = lockingCount.decrementAndGet();
        log.debug("抢锁中线程数-1({})=[{}]", Thread.currentThread().getName(), count);
        if (count == 0) super.handleWhenLockCompleted();
    }

}
//end::class[]
