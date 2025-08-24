package kr.hhplus.be.server.common.lock;

import kr.hhplus.be.server.common.lock.resolver.LockKeyResolver;
import kr.hhplus.be.server.common.vo.AopOrder;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop implements Ordered {
    public static final String LOCK_PREFIX = "lock:";

    private final LockManager lockManager;
    private final ApplicationContext applicationContext;

    @Override
    public int getOrder() {
        return AopOrder.LOCKING.getOrder();
    }

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint,
                       DistributedLock distributedLock) throws Throwable {
        LockKeyResolver resolver = applicationContext.getBean(distributedLock.resolver());
        List<String> partialKeys = resolver.resolve(joinPoint);

        List<String> lockKeys = partialKeys.stream().map(key -> LOCK_PREFIX + key).toList();
        long wait = distributedLock.waitTime();
        long lease = distributedLock.leaseTime();

        return lockManager.lock(wait, lease, lockKeys, joinPoint::proceed);
    }
}
