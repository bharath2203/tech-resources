package org.bgr;

import java.util.function.Supplier;

public interface DistributedLockingService {
    /**
     * Executes the function provided by the @param supplier.
     * Throws ResourceLockedException if the lock is already acquired
     * @param resourceKey - key with which the lock should be acquired.
     * @param supplier - execution function which will be run on the critical section
     * @return Returns T which is the result of the supplier
     * @param <T> Return Type of the supplier
     */
    <T> T execute(String resourceKey, Supplier<T> supplier) throws ResourceLockedException;

    /**
     * Support timeout of the task. Returns default value in case of timeout.
     * @param resourceKey - key with which the lock should be acquired.
     * @param supplier - execution function which will be run on the critical section
     * @param waitTime    - time to wait till lock can be acquired
     * @param leaseTime  - time given for a supplier to run
     * @param defaultValue - default value to be returned on timeout.
     * @return Returns T which is the result of the supplier
     * @param <T> Return Type of the supplier
     */
    <T> T execute(String resourceKey, Supplier<T> supplier, Long waitTime, Long leaseTime, T defaultValue) throws ResourceLockedException;
}
