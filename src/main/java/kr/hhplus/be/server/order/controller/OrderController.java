package kr.hhplus.be.server.order.controller;

import kr.hhplus.be.server.common.response.DataResponse;
import kr.hhplus.be.server.order.controller.docs.OrderApiSpec;
import kr.hhplus.be.server.order.controller.dto.OrderApi;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RequestMapping("/api/v1/orders")
@RestController
public class OrderController implements OrderApiSpec {

    @PostMapping
    public DataResponse<OrderApi.Response> placeOrder(@RequestBody OrderApi.Request request) {
        LocalDateTime localDateTime = LocalDateTime.of(2025, 7, 18, 2, 0, 0);
        return DataResponse.success(
                new OrderApi.Response(
                        1L,
                        request.userId(),
                        1000L,
                        10L,
                        990L,
                        localDateTime
                )
        );
    }
}
