package org.bgr.redis;

import org.bgr.DistributedLockingService;
import org.bgr.ResourceLockedException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


public class RedisDistributedLockingService implements DistributedLockingService {

    RedissonClient redisson;

    public RedisDistributedLockingService(RedissonClient redisson) {
        this.redisson = redisson;
    }

    /**
     * Executes the function provided by the @param supplier.
     * Throws ResourceLockedException if the lock is already acquired
     *
     * @param resourceKey - key with which the lock should be acquired.
     * @param supplier    - execution function which will be run on the critical section
     * @return Returns T which is the result of the supplier
     */
    @Override
    public <T> T execute(String resourceKey, Supplier<T> supplier) throws ResourceLockedException {
        RLock rLock = redisson.getLock(resourceKey);
        if (rLock.tryLock()) {
            try {
                return supplier.get();
            } finally {
                rLock.unlock();
            }
        } else {
            throw new ResourceLockedException();
        }
    }

    /**
     * Support timeout of the task. Returns default value in case of timeout.
     *
     * @param resourceKey  - key with which the lock should be acquired.
     * @param supplier     - execution function which will be run on the critical section
     * @param waitTime    - time to wait till lock can be acquired
     * @param leaseTime  - time given for a supplier to run
     * @param defaultValue - default value to be returned on timeout.
     * @return Returns T which is the result of the supplier
     */
    @Override
    public <T> T execute(String resourceKey, Supplier<T> supplier, Long waitTime, Long leaseTime, T defaultValue) throws ResourceLockedException {
        RLock rLock = redisson.getLock(resourceKey);
        try {
            boolean lock = rLock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
            if (lock) {
                return CompletableFuture
                        .supplyAsync(supplier)
                        .completeOnTimeout(defaultValue, leaseTime, TimeUnit.MILLISECONDS)
                        .get();
            } else {
                throw new ResourceLockedException();
            }
        } catch (InterruptedException e) {
            return defaultValue;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            rLock.unlock();
        }
    }
}
