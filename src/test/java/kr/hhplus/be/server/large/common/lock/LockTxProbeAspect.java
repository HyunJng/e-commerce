package kr.hhplus.be.server.large.common.lock;

import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.lock.DistributedLockAop;
import kr.hhplus.be.server.common.lock.resolver.LockKeyResolver;
import kr.hhplus.be.server.common.vo.AopOrder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Aspect
public class LockTxProbeAspect implements Ordered {
    private final RedissonClient redisson;
    private final ApplicationContext applicationContext;

    public LockTxProbeAspect(RedissonClient redisson, ApplicationContext applicationContext) {
        this.redisson = redisson;
        this.applicationContext = applicationContext;
    }

    @Override
    public int getOrder() {
        return AopOrder.LOGGING.getOrder();
    }

    @Around("@annotation(distributedLock)")
    public Object probe(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        LockKeyResolver resolver = applicationContext.getBean(distributedLock.resolver());
        List<String> keys = resolver.resolve(joinPoint).stream().map(key -> DistributedLockAop.LOCK_PREFIX + key).toList();

        // before
        for (String key : keys) {
            boolean transactionBeforeLock = redisson.getLock(key).isLocked();
            assertThat(transactionBeforeLock).as("락 획득 상태").isTrue();
        }
        boolean txActiveBefore = TransactionSynchronizationManager.isActualTransactionActive();
        assertThat(txActiveBefore).as("트랜잭션 시작 전 상태").isFalse();

        // run
        Object result = joinPoint.proceed();

        // after
        boolean txActiveAfter = TransactionSynchronizationManager.isActualTransactionActive();
        assertThat(txActiveAfter).as("트랜잭션 완료 상태").isFalse();
        for (String key : keys) {
            boolean transactionAfterLock = redisson.getLock(key).isLocked();
            assertThat(transactionAfterLock).as("락 해제 전 상태").isTrue();
        }

        return result;
    }
}

