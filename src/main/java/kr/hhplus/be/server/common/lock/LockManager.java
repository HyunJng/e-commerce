package kr.hhplus.be.server.common.lock;


import java.util.List;

public interface LockManager {

    <T> T lock(long waitTime,
                long leaseTime,
                List<String> lockKeys,
               CustomThrowingSupplier<T> operation
    ) throws Throwable;
}
