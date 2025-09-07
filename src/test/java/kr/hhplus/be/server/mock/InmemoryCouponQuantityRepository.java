package kr.hhplus.be.server.mock;

import kr.hhplus.be.server.coupon.application.port.CouponQuantityRepository;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InmemoryCouponQuantityRepository implements CouponQuantityRepository {

    private final ConcurrentMap<Long, Long> limitByCoupon = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Set<Long>> issuedUsersByCoupon = new ConcurrentHashMap<>();

    public void setLimit(long couponId, long limit) {
        limitByCoupon.put(couponId, limit);
    }
    public void clearAll() {
        limitByCoupon.clear();
        issuedUsersByCoupon.clear();
    }

    @Override
    public Long getLimit(Long couponId) {
        return limitByCoupon.getOrDefault(couponId, 0L);
    }

    @Override
    public Long getIssuedUsersCount(Long couponId) {
        return (long) issuedUsersByCoupon.getOrDefault(couponId, Collections.emptySet()).size();
    }

    @Override
    public boolean isAlreadyIssued(Long couponId, Long userId) {
        return issuedUsersByCoupon.getOrDefault(couponId, Collections.emptySet()).contains(userId);
    }

    @Override
    public void markIssued(long couponId, long userId) {
        issuedUsersByCoupon.computeIfAbsent(couponId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }
}
