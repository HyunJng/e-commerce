package kr.hhplus.be.server.large.coupon;

import kr.hhplus.be.server.coupon.application.usecase.IssueCouponUseCase;
import kr.hhplus.be.server.large.AbstractConcurrencyTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/coupon-concurrency-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
class IssueCouponUseCaseConcurrencyTest extends AbstractConcurrencyTest {

    @Autowired
    private IssueCouponUseCase issueCouponUseCase;

    @Test
    void 동시에_150건의_쿠폰발급_요청이_들어와도_정확히_100건만_발급된다() throws Exception {
        // given
        Long couponId = 1L;

        // when
        List<Boolean> results = AbstractConcurrencyTest.runConcurrentTest(8, 150, i -> {
            final Long userId = (long) i + 1;
            IssueCouponUseCase.Input input = new IssueCouponUseCase.Input(couponId, userId);
            IssueCouponUseCase.Output output = issueCouponUseCase.execute(input);
            return output.isSuccess();
        });

        // then
        long successCount = results.stream().filter(result -> result).count();
        assertThat(successCount).isEqualTo(100);
    }
}