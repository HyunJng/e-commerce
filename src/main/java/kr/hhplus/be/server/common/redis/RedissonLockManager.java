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
        RLock rLock = getRLock(lockKeys);
        boolean available = false;
        try {
            available = rLock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            if (!available) {
                throw new CommonException(ErrorCode.FAIL_LOCK_ACQUIRED_TIME);
            }

            return operation.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommonException(ErrorCode.FAIL_LOCK_ACQUIRED_TIME);
        } finally {
            if (available && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    private RLock getRLock(List<String> lockKeys) {
        if (lockKeys == null || lockKeys.isEmpty()) throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);

        if (lockKeys.size() == 1) {
            return redissonClient.getLock(lockKeys.get(0));
        }
        RLock[] rLocks = lockKeys.stream().map(redissonClient::getLock).toArray(RLock[]::new);
        return redissonClient.getMultiLock(rLocks);
    }
}
