package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.repository.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.repository.IssuedCouponLockLoader;
import kr.hhplus.be.server.order.domain.entity.DiscountInfo;
import kr.hhplus.be.server.order.domain.entity.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponPricingService {

    private final CouponJpaRepository couponJpaRepository;
    private final IssuedCouponLockLoader issuedCouponLockLoader;
    private final DateHolder dateHolder;

    public DiscountInfo applyCouponPricing(Long couponId, Long userId, List<OrderItem> items) {
        if (couponId == null) {
            return DiscountInfo.none();
        }

        Coupon coupon = couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "쿠폰"));

        IssuedCoupon issuedCoupon = issuedCouponLockLoader.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "발급 쿠폰"));

        issuedCoupon.validate(dateHolder);
        issuedCoupon.use();

        Long totalAmount = items.stream().mapToLong(OrderItem::totalAmount).sum();
        Long discountAmount = coupon.calculateDiscountAmount(totalAmount);

        return new DiscountInfo(discountAmount, issuedCoupon.getId());
    }
}
