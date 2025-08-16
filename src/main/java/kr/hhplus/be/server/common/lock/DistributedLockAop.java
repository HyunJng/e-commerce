package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class DistributedLockAop {
    public static final String LOCK_PREFIX = "lock:";

    private final LockManager lockManager;
    private final ApplicationContext applicationContext;

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
