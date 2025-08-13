package kr.hhplus.be.server.common.redis;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.lock.LockManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonLockManager implements LockManager {

    private final RedissonClient redissonClient;

    public Object lock(long waitTime,
                       long leaseTime,
                       List<String> lockKeys,
                       Supplier<Object> operation) {
        List<RLock> acquired = new ArrayList<>(lockKeys.size());
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

    private void unlockReverse(List<RLock> acquired) {
        for (RLock rLock : acquired) {
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
