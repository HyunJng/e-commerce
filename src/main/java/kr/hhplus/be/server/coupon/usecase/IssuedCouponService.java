package kr.hhplus.be.server.coupon.usecase;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.coupon.domain.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.IssuedCouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class IssuedCouponService {

    public record Input(Long couponId, Long userId) {
    }

    public record Output(
        Long id,
        Long couponId,
        String couponName,
        Long discountAmount,
        String discountType,
        LocalDate startedAt,
        LocalDate endAt
    ) {
        public static Output from(IssuedCoupon issuedCoupon, Coupon coupon) {
            return new Output(
                issuedCoupon.getId(),
                issuedCoupon.getCouponId(),
                coupon.getName(),
                coupon.getDiscountAmount(),
                coupon.getDiscountType().name(),
                issuedCoupon.getStartAt(),
                issuedCoupon.getEndAt()
            );
        }
    }

    private final CouponJpaRepository couponJpaRepository;
    private final IssuedCouponJpaRepository issuedCouponJpaRepository;
    private final DateHolder dateHolder;

    public Output execute(Input input) {
        // 유저정보의 유효성은 인증/인가 과정에서 처리되었을 것이라 가정하였음
        Coupon coupon = couponJpaRepository.findById(input.couponId)
            .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "쿠폰"));

        IssuedCoupon issuedCoupon = new IssuedCoupon(coupon, input.userId, dateHolder);

        IssuedCoupon savedIssuedCoupon = issuedCouponJpaRepository.save(issuedCoupon);

        return Output.from(savedIssuedCoupon, coupon);
    }
}
