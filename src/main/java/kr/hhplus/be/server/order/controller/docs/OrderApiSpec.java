package kr.hhplus.be.server.order.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.order.controller.dto.OrderApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Order", description = "주문/결제 API")
public interface OrderApiSpec {

    @Operation(
            summary = "주문 및 결제",
            description = "사용자가 상품을 주문하고 결제합니다. 쿠폰은 선택적으로 포함할 수 있습니다."
    )
    ResponseEntity<OrderApi.Response> placeOrder(@RequestBody OrderApi.Request request);

}