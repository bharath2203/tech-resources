package org.bgr;

import org.bgr.redis.RedisDistributedLockingService;
import org.redisson.Redisson;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class DistributedLockingServiceTestRunner {
    static AtomicInteger atomicInteger = new AtomicInteger(0);

    private static Supplier<Integer> integerSupplier() {
        return () -> {
            atomicInteger.incrementAndGet();
            System.out.println("Executing supplier. Thread name=" + Thread.currentThread().getName() + ", counter=" + atomicInteger.get());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Executed supplier. Thread name=" + Thread.currentThread().getName() + ", counter=" + atomicInteger.get());
            return atomicInteger.get();
        };
    }

    public static void main(String[] args) throws ResourceLockedException, ExecutionException, InterruptedException {

        RedisDistributedLockingService redisDistributedLockingService = new RedisDistributedLockingService(
                Redisson.create()
        );

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        executorService.submit(() -> redisDistributedLockingService.execute("key1", integerSupplier()));
        executorService.submit(() -> redisDistributedLockingService.execute("key2", integerSupplier()));
        executorService.submit(() -> redisDistributedLockingService.execute("key1", integerSupplier())).get();
    }
}
