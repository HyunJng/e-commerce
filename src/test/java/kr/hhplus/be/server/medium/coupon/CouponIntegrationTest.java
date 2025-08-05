package kr.hhplus.be.server.medium.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.coupon.presentation.dto.CouponIssueApi;
import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.coupon.domain.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.IssuedCouponJpaRepository;
import kr.hhplus.be.server.medium.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/coupon-integration-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
public class CouponIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private IssuedCouponJpaRepository issuedCouponJpaRepository;
    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Test
    void 쿠폰_발급에_성공하면_발급_정보가_저장된다() throws Exception {
        // given
        Long couponId = 1L;
        Long userId = 1L;

        Coupon coupon = couponJpaRepository.findById(couponId).get();

        CouponIssueApi.Request request = new CouponIssueApi.Request(userId, couponId);
        String content = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/api/v1/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        // then
        IssuedCoupon issuedCoupon = issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId).orElseGet(null);

        assertThat(issuedCoupon).isNotNull();
        assertThat(issuedCoupon.getUserId()).isEqualTo(userId);
        assertThat(issuedCoupon.getCouponId()).isEqualTo(couponId);
        assertThat(issuedCoupon.getStartDate().plusDays(coupon.getDates())).isEqualTo(issuedCoupon.getEndDate());
        assertThat(issuedCoupon.getStatus()).isEqualTo(IssuedCoupon.Status.ACTIVE);
    }

    @Test
    void 쿠폰_발급에_실패하면_해당_유저와_쿠폰정보가_매칭된_발급정보가_존재하지_않아야한다() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = 999L;

        CouponIssueApi.Request request = new CouponIssueApi.Request(userId, couponId);
        String content = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/api/v1/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());

        // then
        IssuedCoupon issuedCoupon = issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId).orElse(null);

        assertThat(issuedCoupon).isNull();
    }
}

