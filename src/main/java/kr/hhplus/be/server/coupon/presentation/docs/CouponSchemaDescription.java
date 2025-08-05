package kr.hhplus.be.server.coupon.presentation.docs;

public interface CouponSchemaDescription {

    String userId = "사용자 ID";
    String couponId = "쿠폰 ID";
    String issuedId = "발급된 쿠폰 ID";
    String couponName = "쿠폰 이름";
    String discountAmount = "할인 금액";
    String discountType = "할인 타입";
    String startedAt = "사용 시작일";
    String endAt = "사용 마감일";

}
