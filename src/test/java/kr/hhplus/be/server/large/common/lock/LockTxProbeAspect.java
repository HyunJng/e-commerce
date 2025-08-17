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
        List<String> keys = resolver.resolve(joinPoint);

        String key = DistributedLockAop.LOCK_PREFIX + keys.get(0);

        boolean txActiveBefore = TransactionSynchronizationManager.isActualTransactionActive();
        boolean transactionBeforeLock = redisson.getLock(key).isLocked();

        assertThat(transactionBeforeLock).as("락 획득 상태").isTrue();
        assertThat(txActiveBefore).as("트랜잭션 시작 전 상태").isFalse();

        Object result = joinPoint.proceed();

        boolean txActiveAfter = TransactionSynchronizationManager.isActualTransactionActive();
        boolean transactionAfterLock = redisson.getLock(key).isLocked();
        assertThat(txActiveAfter).as("트랜잭션 완료 상태").isFalse();
        assertThat(transactionAfterLock).as("락 해제 전 상태").isTrue();

        return result;
    }
}

