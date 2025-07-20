package kr.hhplus.be.server.small.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.order.controller.OrderController;
import kr.hhplus.be.server.order.controller.dto.OrderApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static kr.hhplus.be.server.mock.ControllerTestFixtures.기본_성공_포맷_검증;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(value = OrderController.class)
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 주문을_성공하면_주문정보와_사용금액을_응답한다() throws Exception {
        //given
        OrderApi.Request.OrderProduct orderProduct1 = new OrderApi.Request.OrderProduct(1L, 1);
        OrderApi.Request.OrderProduct orderProduct2 = new OrderApi.Request.OrderProduct(2L, 1);
        OrderApi.Request request = new OrderApi.Request(1L, 2L, List.of(orderProduct1, orderProduct2));

        String content = objectMapper.writeValueAsString(request);

        //when & then
        기본_성공_포맷_검증(
                mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
        )
                .andExpect(jsonPath("$.result.orderId").isNumber())
                .andExpect(jsonPath("$.result.userId").value(request.userId()))
                .andExpect(jsonPath("$.result.totalAmount").isNumber())
                .andExpect(jsonPath("$.result.discountAmount").isNumber())
                .andExpect(jsonPath("$.result.paidAmount").isNumber())
                .andExpect(jsonPath("$.result.createAt").isNotEmpty());
    }
}