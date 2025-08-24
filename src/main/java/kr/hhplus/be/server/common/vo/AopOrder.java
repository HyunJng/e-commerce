package kr.hhplus.be.server.common.vo;

import lombok.Getter;

@Getter
public enum AopOrder {

    // HIGHEST_PRECEDENCE
    LOCKING(100),
    LOGGING(1000);
    // LOWEST_PRECEDENCE

    private final int order;
    AopOrder(int order) {
        this.order = order;
    }
}
