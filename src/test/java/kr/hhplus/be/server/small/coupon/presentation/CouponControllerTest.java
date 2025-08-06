package kr.hhplus.be.server.small.coupon.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.exception.GeneralExceptionAdvice;
import kr.hhplus.be.server.coupon.application.usecase.IssuedCouponUseCase;
import kr.hhplus.be.server.coupon.presentation.controller.CouponController;
import kr.hhplus.be.server.coupon.presentation.dto.CouponIssueApi;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {CouponController.class})
@Import(value = {GeneralExceptionAdvice.class})
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IssuedCouponUseCase issuedCouponUseCase;

    @Test
    void 쿠폰_발급에_성공하면_200응답과_발급된_쿠폰정보를_반환한다() throws Exception {
        // given
        Long couponId = 1L;
        Long userId = 1L;
        CouponIssueApi.Request request = new CouponIssueApi.Request(userId, couponId);
        String content = objectMapper.writeValueAsString(request);

        IssuedCouponUseCase.Input input = new IssuedCouponUseCase.Input(couponId, userId);

        BDDMockito.given(issuedCouponUseCase.execute(input))
                .willReturn(new IssuedCouponUseCase.Output(true));

        // when & then
        mockMvc.perform(post("/api/v1/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

}