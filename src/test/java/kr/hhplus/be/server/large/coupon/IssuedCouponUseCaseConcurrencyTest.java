package kr.hhplus.be.server.large.coupon;

import kr.hhplus.be.server.coupon.application.usecase.IssuedCouponUseCase;
import kr.hhplus.be.server.coupon.domain.entity.CouponQuantity;
import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.repository.CouponQuantityJpaRepository;
import kr.hhplus.be.server.coupon.domain.repository.IssuedCouponJpaRepository;
import kr.hhplus.be.server.large.AbstractConCurrencyTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/coupon-concurrency-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
class IssuedCouponUseCaseConcurrencyTest extends AbstractConCurrencyTest {

    @Autowired
    private IssuedCouponUseCase issuedCouponUseCase;

    @Autowired
    private IssuedCouponJpaRepository issuedCouponJpaRepository;
    @Autowired
    private CouponQuantityJpaRepository couponQuantityJpaRepository;

    @Test
    void 동시에_150건의_요청이_들어와도_정확히_100건만_발급된다() throws Exception {
        // given
        Long couponId = 1L;

        // when
        AbstractConCurrencyTest.runConcurrentTest(150, i -> {
            final Long userId = (long) i + 1;
            IssuedCouponUseCase.Input input = new IssuedCouponUseCase.Input(couponId, userId);
            issuedCouponUseCase.execute(input);
        });

        // then
        await().atMost(2, SECONDS)
                .untilAsserted(() -> {
                    List<IssuedCoupon> issuedCoupons = issuedCouponJpaRepository.findAll();
                    assertThat(issuedCoupons.size()).isEqualTo(100);
                });
        CouponQuantity couponQuantity = couponQuantityJpaRepository.findById(couponId).orElse(null);
        assertThat(couponQuantity.getIssuedQuantity()).isEqualTo(100);
    }
}