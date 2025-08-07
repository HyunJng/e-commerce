package kr.hhplus.be.server.order.domain.entity;

public record DiscountInfo(Long discountAmount, Long issuedCouponId) {
    public static DiscountInfo none() {
        return new DiscountInfo(0L, null);
    }
}