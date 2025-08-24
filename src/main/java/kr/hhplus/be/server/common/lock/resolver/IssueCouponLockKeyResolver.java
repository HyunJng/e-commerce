package kr.hhplus.be.server.common.lock.resolver;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IssueCouponLockKeyResolver implements LockKeyResolver {

    @Override
    public List<String> resolve(ProceedingJoinPoint joinPoint) {
        List<String> keys = new ArrayList<>();

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long couponId) {
                keys.add("coupon:drain:" + couponId);
            }
        }
        return keys;
    }
}
