package kr.hhplus.be.server.common.lock;


import kr.hhplus.be.server.common.functional.CustomThrowingSupplier;

import java.util.List;

public interface LockManager {

    <T> T lock(long waitTime,
                long leaseTime,
                List<String> lockKeys,
               CustomThrowingSupplier<T> operation
    ) throws Throwable;
}
