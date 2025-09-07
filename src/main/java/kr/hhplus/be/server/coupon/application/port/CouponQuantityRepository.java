package kr.hhplus.be.server.coupon.application.port;

public interface CouponQuantityRepository {

    Long getLimit(Long couponId);

    Long getIssuedUsersCount(Long couponId);

    boolean isAlreadyIssued(Long couponId, Long userId);

    void markIssued(long couponId, long userId);

}
