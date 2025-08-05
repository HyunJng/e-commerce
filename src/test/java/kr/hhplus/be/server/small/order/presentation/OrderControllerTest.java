package kr.hhplus.be.server.small.order.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.GeneralExceptionAdvice;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.order.presentation.controller.OrderController;
import kr.hhplus.be.server.order.presentation.dto.OrderApi;
import kr.hhplus.be.server.order.application.usecase.PlaceOrderUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {OrderController.class})
@Import(value = {GeneralExceptionAdvice.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlaceOrderUseCase placeOrderUseCase;

    @Test
    void 주문에_성공하면_200응답과_주문정보를_반환한다() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        OrderApi.Request request = new OrderApi.Request(
                userId,
                couponId,
                List.of(new OrderApi.Request.OrderProduct(1L, 10))
        );

        String content = objectMapper.writeValueAsString(request);

        PlaceOrderUseCase.Output output = new PlaceOrderUseCase.Output(
                1L,
                userId,
                2000L,
                500L,
                1500L,
                null
        );
        given(placeOrderUseCase.execute(request.to())).willReturn(output);

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(output.orderId()))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalAmount").value(output.totalAmount()))
                .andExpect(jsonPath("$.discountAmount").value(output.discountAmount()))
                .andExpect(jsonPath("$.paidAmount").value(output.paidAmount()));
    }

    @Test
    void 상품이_존재하지_않으면_400응답을_반환한다() throws Exception {
        // given
        Long userId = 1L;
        OrderApi.Request request = new OrderApi.Request(
                userId,
                null,
                List.of(new OrderApi.Request.OrderProduct(999L, 1))
        );
        String content = objectMapper.writeValueAsString(request);

        given(placeOrderUseCase.execute(request.to()))
                .willThrow(new CommonException(ErrorCode.INVALID_REQUEST, "주문한 상품 중 일부가 존재하지 않습니다."));

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultMsg").value(ErrorCode.INVALID_REQUEST.getMessage("주문한 상품 중 일부가 존재하지 않습니다.")));
    }

    @Test
    void 쿠폰이_존재하지_않으면_400응답을_반환한다() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = 999L;
        OrderApi.Request request = new OrderApi.Request(
                userId,
                couponId,
                List.of(new OrderApi.Request.OrderProduct(1L, 1))
        );
        String content = objectMapper.writeValueAsString(request);

        given(placeOrderUseCase.execute(request.to()))
                .willThrow(new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "쿠폰"));

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultMsg").value(ErrorCode.NOT_FOUND_RESOURCE.getMessage("쿠폰")));
    }
}