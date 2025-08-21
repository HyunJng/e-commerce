package kr.hhplus.be.server.coupon.application.port;

import java.util.List;

public interface CouponQuantityRepository {

    Long getLimit(Long couponId);

    Long getIssuedUsersCount(Long couponId);

    boolean isAlreadyIssued(Long couponId, Long userId);

    boolean enqueue(long couponId, long userId, long nowMillis);

    List<Long> popNext(long couponId, int n);

    void markIssued(long couponId, long userId);

}
