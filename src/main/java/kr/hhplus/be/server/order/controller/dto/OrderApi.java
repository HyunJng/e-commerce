package kr.hhplus.be.server.order.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.order.controller.docs.OrderSchemaDescription;

import java.time.LocalDateTime;

public class OrderApi {

    public record Request(
            @Schema(description = OrderSchemaDescription.userId) Long userId,
            @Schema(description = OrderSchemaDescription.productId) Long productId,
            @Schema(description = OrderSchemaDescription.quantity) Integer quantity,
            @Schema(description = OrderSchemaDescription.couponId) Long couponId
    ) {}

    public record Response(
            @Schema(description = OrderSchemaDescription.orderId) Long orderId,
            @Schema(description = OrderSchemaDescription.userId) Long userId,
            @Schema(description = OrderSchemaDescription.totalAmount) Long totalAmount,
            @Schema(description = OrderSchemaDescription.discountAmount) Long discountAmount,
            @Schema(description = OrderSchemaDescription.paidAmount) Long paidAmount,
            @Schema(description = OrderSchemaDescription.createAt) LocalDateTime createAt
    ) {}
}
