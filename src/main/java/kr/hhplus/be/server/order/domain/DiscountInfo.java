package kr.hhplus.be.server.order.domain;

public record DiscountInfo(Long discountAmount, Long issuedCouponId) {
    public static DiscountInfo none() {
        return new DiscountInfo(0L, null);
    }
}