package kr.hhplus.be.server.medium.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.event.KafkaTopicsProperties;
import kr.hhplus.be.server.coupon.application.port.CouponQuantityRepository;
import kr.hhplus.be.server.coupon.domain.repository.IssuedCouponJpaRepository;
import kr.hhplus.be.server.coupon.presentation.dto.CouponIssueApi;
import kr.hhplus.be.server.medium.AbstractIntegrationTest;
import kr.hhplus.be.server.mock.InmemoryCouponQuantityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/coupon-concurrency-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
public class IssueCouponIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private IssuedCouponJpaRepository issuedCouponJpaRepository;
    @Autowired
    private CouponQuantityRepository couponQuantityRepository;
    @Autowired
    KafkaTopicsProperties kafkaTopicsProperties;

    private final long couponId = 1L;

    @BeforeEach
    void init() {
        if (couponQuantityRepository instanceof InmemoryCouponQuantityRepository mem) {
            mem.clearAll();
            mem.setLimit(couponId, 5L);
        }
    }

    private boolean postIssue(long couponId, long userId) throws Exception {
        var req = new CouponIssueApi.Request(userId, couponId);

        var mvcResult = mockMvc.perform(
                        post("/api/v1/coupons/issue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andReturn();

        var body = mvcResult.getResponse().getContentAsString();
        var resp = objectMapper.readValue(body, CouponIssueApi.Response.class);
        return resp.isSuccess();
    }

    @Test
    void 첫_요청이면_쿠폰_발급에_성공한다() throws Exception {
        long userId = 100L;

        boolean accepted = postIssue(couponId, userId);
        assertThat(accepted).isTrue();

        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() ->
                        assertThat(issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId)).isNotEmpty()
                );
    }

    @Test
    void 이미_발급된_사용자는_실패를_응답한다() throws Exception {
        long userId = 100L;

        boolean accepted1 = postIssue(couponId, userId);
        assertThat(accepted1).isTrue();

        await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() ->
                        assertThat(issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId)).isNotEmpty()
                );

        boolean accepted2 = postIssue(couponId, userId);

        assertThat(accepted2).isFalse();

        await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() ->
                        assertThatCode(() -> issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId))
                                .doesNotThrowAnyException()
                );
    }
}