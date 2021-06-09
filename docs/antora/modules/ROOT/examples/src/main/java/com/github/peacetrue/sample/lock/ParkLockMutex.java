package com.github.peacetrue.sample.lock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 加锁和解锁之间互斥
 *
 * @author : xiayx
 * @since : 2021-06-03 11:50
 **/
//tag::class[]
@Getter
@Slf4j
public class ParkLockMutex extends ParkLock {

    public static final int STATE_NORMAL = 0;
    public static final int STATE_LOCKING = 1;
    public static final int STATE_UNLOCKING = 2;

    /** 状态：0 常规、1 抢锁中、2 解锁中 */
    private final AtomicInteger state = new AtomicInteger(0);

    /** 是否抢锁中 */
    protected boolean isLocking() {
        return getState().get() == STATE_LOCKING;
    }

    @Override
    public void lock() {
        if (canLock()) {
            super.lock();
        }
    }

    /** 是否能加锁 */
    protected boolean canLock() {
        return setLocking();
    }

    /** 设置为抢锁中 */
    protected boolean setLocking() {
        while (true) {
            if (state.compareAndSet(STATE_NORMAL, STATE_LOCKING)) {
                log.debug("线程[{}]设置状态为[抢锁中]", Thread.currentThread().getName());
                return true;
            }
        }
    }

    @Override
    protected void handleWhenLockCompleted() {
        log.debug("抢锁完后，线程[{}]设置状态为[常规]", Thread.currentThread().getName());
        if (!state.compareAndSet(STATE_LOCKING, STATE_NORMAL)) {
            throw new IllegalStateException(String.format("抢锁状态[%s]设置为常规状态失败！", state.get()));
        }
    }

    @Override
    protected void doUnlock() {
        if (canUnlock()) super.doUnlock();
    }

    /** 是否能解锁 */
    protected boolean canUnlock() {
        return setUnlocking();
    }

    /** 设置为解锁中 */
    protected boolean setUnlocking() {
        while (true) {
            if (state.compareAndSet(STATE_NORMAL, STATE_UNLOCKING)) {
                log.debug("线程[{}]设置状态为[解锁中]", Thread.currentThread().getName());
                return true;
            }
        }
    }

    @Override
    protected void handleWhenUnlockCompleted() {
        log.debug("解锁完后，线程[{}]设置状态为[常规]", Thread.currentThread().getName());
        if (!state.compareAndSet(STATE_UNLOCKING, STATE_NORMAL)) {
            throw new IllegalStateException(String.format("解锁状态[%s]设置为常规状态失败！", state.get()));
        }
    }
}
//end::class[]
