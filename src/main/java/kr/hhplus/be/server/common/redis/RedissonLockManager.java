package kr.hhplus.be.server.common.redis;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.functional.CustomThrowingSupplier;
import kr.hhplus.be.server.common.lock.LockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonLockManager implements LockManager {

    private final RedissonClient redissonClient;

    public <T> T lock(long waitTime,
                       long leaseTime,
                       List<String> lockKeys,
                      CustomThrowingSupplier<T> operation
    ) throws Throwable {
        Deque<RLock> acquired = new ArrayDeque<>();
        try {
            for (String lockKey : lockKeys) {
                RLock rLock = redissonClient.getLock(lockKey);
                boolean available = rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
                if (!available) {
                    throw new CommonException(ErrorCode.FAIL_LOCK_ACQUIRED_TIME);
                }
                acquired.add(rLock);
            }
            return operation.get();
        } catch (InterruptedException e) {
            throw new CommonException(ErrorCode.FAIL_LOCK_ACQUIRED_TIME);
        } finally {
            unlockReverse(acquired);
        }
    }

    private void unlockReverse(Deque<RLock> acquired) {
        while (!acquired.isEmpty()) {
            RLock rLock = acquired.pop();
            try {
                if (rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                }
            } catch (IllegalMonitorStateException ignore) {
            } catch (Exception e) {
                log.error("FAIL UNLOCK {}", rLock.getName(), e);
            }
        }
    }

}
