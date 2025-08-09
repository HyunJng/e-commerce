package kr.hhplus.be.server.medium.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.repository.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.repository.IssuedCouponJpaRepository;
import kr.hhplus.be.server.coupon.presentation.dto.CouponIssueApi;
import kr.hhplus.be.server.medium.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

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
        await().atMost(2, SECONDS)
                .untilAsserted(() -> {
                    IssuedCoupon issuedCoupon =
                            issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId).orElse(null);
                    assertThat(issuedCoupon).isNotNull();
                    assertThat(issuedCoupon.getUserId()).isEqualTo(userId);
                    assertThat(issuedCoupon.getCouponId()).isEqualTo(couponId);
                    assertThat(issuedCoupon.getStartDate().plusDays(coupon.getDates())).isEqualTo(issuedCoupon.getEndDate());
                    assertThat(issuedCoupon.getStatus()).isEqualTo(IssuedCoupon.Status.ACTIVE);
                });
    }
}

