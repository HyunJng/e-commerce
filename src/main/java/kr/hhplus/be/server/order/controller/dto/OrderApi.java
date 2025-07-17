package kr.hhplus.be.server.order.controller.dto;

import java.time.LocalDateTime;

public class OrderApi {

    public record Request(
            Long userId,
            Long productId,
            Integer quantity,
            Long couponId
    ) {}

    public record Response(
            Long orderId,
            Long userId,
            Long totalAmount,
            Long discountAmount,
            Long paidAmount,
            LocalDateTime createAt
    ) {}
}
