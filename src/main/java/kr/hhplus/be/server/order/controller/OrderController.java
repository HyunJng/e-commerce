package kr.hhplus.be.server.order.controller;

import kr.hhplus.be.server.order.controller.docs.OrderApiSpec;
import kr.hhplus.be.server.order.controller.dto.OrderApi;
import kr.hhplus.be.server.order.usecase.PlaceOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController implements OrderApiSpec {

    private final PlaceOrderService placeOrderService;

    @PostMapping
    public ResponseEntity<OrderApi.Response> placeOrder(@RequestBody OrderApi.Request request) {
        PlaceOrderService.Output output = placeOrderService.execute(request.to());

        return ResponseEntity.ok(OrderApi.Response.from(output));
    }
}
