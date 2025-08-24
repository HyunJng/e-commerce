package kr.hhplus.be.server.common.redis;

import lombok.Getter;

@Getter
public enum RedisKey {
    BEST_PRODUCT_RANKING("best-product"),
    BEST_PRODUCT_AGGREGATE("best-product:aggregate"),
    BEST_PRODUCT_AGGREGATE_TEMP("best-product:aggregate:temp"),

    COUPON_ISSUED_LIMIT("coupon:%1:limit"),
    COUPON_ISSUED_USERS("coupon:%1:issued-users"),
    COUPON_ISSUED_QUEUE("coupon:%1:issued-queue");

    private final String key;
    RedisKey(String key) {
        this.key = key;
    }
}
