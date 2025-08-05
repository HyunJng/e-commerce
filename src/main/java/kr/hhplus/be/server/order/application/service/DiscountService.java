package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.coupon.domain.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.IssuedCouponJpaRepository;
import kr.hhplus.be.server.order.domain.DiscountInfo;
import kr.hhplus.be.server.order.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final CouponJpaRepository couponJpaRepository;
    private final IssuedCouponJpaRepository issuedCouponJpaRepository;
    private final DateHolder dateHolder;

    public void validateOrThrow(Long couponId, Long userId) {
        if (couponId == null) return;

        IssuedCoupon issuedCoupon = issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "쿠폰"));
        issuedCoupon.validate(dateHolder);
    }

    public DiscountInfo calculate(Long couponId, Long userId, List<OrderItem> items) {
        if (couponId == null) {
            return DiscountInfo.none();
        }

        Coupon coupon = couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "쿠폰"));

        IssuedCoupon issuedCoupon = issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "발급 쿠폰"));

        Long totalAmount = items.stream().mapToLong(OrderItem::totalAmount).sum();
        Long discountAmount = coupon.calculateDiscountAmount(totalAmount);

        issuedCoupon.use();
        return new DiscountInfo(discountAmount, issuedCoupon.getId());
    }
}
