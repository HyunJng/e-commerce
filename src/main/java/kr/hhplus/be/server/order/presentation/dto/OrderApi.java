package kr.hhplus.be.server.order.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.order.presentation.docs.OrderSchemaDescription;
import kr.hhplus.be.server.order.application.usecase.PlaceOrderUseCase;

import java.time.LocalDateTime;
import java.util.List;

public class OrderApi {

    public record Request(
            @Schema(description = OrderSchemaDescription.userId) Long userId,
            @Schema(description = OrderSchemaDescription.couponId) Long couponId,
            @Schema(description = OrderSchemaDescription.orderProduct) List<OrderProduct> orderProduct
            ) {

        public record OrderProduct(
                @Schema(description = OrderSchemaDescription.productId) Long productId,
                @Schema(description = OrderSchemaDescription.quantity) Integer quantity
        ) {

        }

        public PlaceOrderUseCase.Input to() {
            return new PlaceOrderUseCase.Input(
                    userId,
                    couponId,
                    orderProduct.stream()
                            .map(product -> new kr.hhplus.be.server.coupon.domain.entity.OrderProduct(product.productId, product.quantity))
                            .toList()
            );
        }
    }

    public record Response(
            @Schema(description = OrderSchemaDescription.orderId) Long orderId,
            @Schema(description = OrderSchemaDescription.userId) Long userId,
            @Schema(description = OrderSchemaDescription.totalAmount) Long totalAmount,
            @Schema(description = OrderSchemaDescription.discountAmount) Long discountAmount,
            @Schema(description = OrderSchemaDescription.paidAmount) Long paidAmount,
            @Schema(description = OrderSchemaDescription.createAt) LocalDateTime createAt
    ) {
        public static Response from(PlaceOrderUseCase.Output output) {
            return new Response(
                    output.orderId(),
                    output.userId(),
                    output.totalAmount(),
                    output.discountAmount(),
                    output.paidAmount(),
                    output.createAt()
            );
        }
    }
}
