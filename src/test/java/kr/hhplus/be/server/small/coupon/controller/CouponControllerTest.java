package kr.hhplus.be.server.small.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.exception.GeneralExceptionAdvice;
import kr.hhplus.be.server.coupon.controller.CouponController;
import kr.hhplus.be.server.coupon.controller.dto.CouponIssueApi;
import kr.hhplus.be.server.coupon.usecase.IssuedCouponService;
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
    private IssuedCouponService issuedCouponService;

    @Test
    void 쿠폰_발급에_성공하면_200응답과_발급된_쿠폰정보를_반환한다() throws Exception {
        // given
        Long couponId = 1L;
        Long userId = 1L;
        CouponIssueApi.Request request = new CouponIssueApi.Request(userId, couponId);
        String content = objectMapper.writeValueAsString(request);

        IssuedCouponService.Input input = new IssuedCouponService.Input(couponId, userId);

        BDDMockito.given(issuedCouponService.execute(input))
                .willReturn(new IssuedCouponService.Output(
                        1L,
                        couponId,
                        "테스트 쿠폰",
                        1000L,
                        "FIXED",
                        null, null
                ));

        // when & then
        mockMvc.perform(post("/api/v1/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.couponId").value(couponId))
                .andExpect(jsonPath("$.couponName").value("테스트 쿠폰"))
                .andExpect(jsonPath("$.discountAmount").value(1000L))
                .andExpect(jsonPath("$.discountType").value("FIXED"));
    }

}