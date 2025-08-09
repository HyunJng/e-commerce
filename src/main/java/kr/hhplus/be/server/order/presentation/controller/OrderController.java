package kr.hhplus.be.server.order.presentation.controller;

import kr.hhplus.be.server.order.presentation.docs.OrderApiSpec;
import kr.hhplus.be.server.order.presentation.dto.OrderApi;
import kr.hhplus.be.server.order.application.usecase.PlaceOrderUseCase;
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

    private final PlaceOrderUseCase placeOrderUseCase;

    @PostMapping
    public ResponseEntity<OrderApi.Response> placeOrder(@RequestBody OrderApi.Request request) {
        PlaceOrderUseCase.Output output = placeOrderUseCase.execute(request.to());

        return ResponseEntity.ok(OrderApi.Response.from(output));
    }
}
