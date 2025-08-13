package kr.hhplus.be.server.common.lock;

import java.util.List;
import java.util.function.Supplier;

public interface LockManager {

    Object lock(long waitTime,
                long leaseTime,
                List<String> lockKeys,
                Supplier<Object> operation);
}
