package kr.hhplus.be.server.small.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.coupon.controller.CouponController;
import kr.hhplus.be.server.coupon.controller.dto.CouponIssueApi;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static kr.hhplus.be.server.mock.ControllerTestFixtures.기본_성공_포맷_검증;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(value = CouponController.class)
@AutoConfigureMockMvc
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 유정정보와_쿠폰정보를_요청하여_쿠폰발급에_성공하면_쿠폰정보를_응답한다() throws Exception {
        //given
        CouponIssueApi.Request request = new CouponIssueApi.Request(1L, 2L);
        String content = objectMapper.writeValueAsString(request);

        //when & then
        기본_성공_포맷_검증(mockMvc.perform(post("/api/v1/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                )
                .andExpect(jsonPath("$.result.id").value(request.userId()))
                .andExpect(jsonPath("$.result.couponId").value(request.couponId()))
                .andExpect(jsonPath("$.result.couponName").value("회원가입쿠폰")) //TODO: 변경 필요
                .andExpect(jsonPath("$.result.discountAmount").value(10)) //TODO: 변경 필요
                .andExpect(jsonPath("$.result.discountType").value("PERCENT")) //TODO: 변경 필요
                .andExpect(jsonPath("$.result.startedAt").value("2025-07-18T02:00:00")) //TODO: 변경 필요
                .andExpect(jsonPath("$.result.endAt").value("2025-07-25T02:00:00"))); //TODO: 변경 필요
    }
}