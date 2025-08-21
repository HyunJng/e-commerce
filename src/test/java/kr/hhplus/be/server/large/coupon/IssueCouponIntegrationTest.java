package kr.hhplus.be.server.large.coupon;

import kr.hhplus.be.server.common.redis.RedisKey;
import kr.hhplus.be.server.common.redis.RedisKeyResolver;
import kr.hhplus.be.server.coupon.application.usecase.IssueCouponUseCase;
import kr.hhplus.be.server.coupon.domain.entity.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.repository.IssuedCouponJpaRepository;
import kr.hhplus.be.server.coupon.infrastructure.IssueCouponQueueScheduler;
import kr.hhplus.be.server.large.AbstractConcurrencyTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static org.assertj.core.api.Assertions.assertThat;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/coupon-concurrency-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
class IssueCouponIntegrationTest extends AbstractConcurrencyTest {

    @Autowired
    private IssueCouponUseCase issueCouponUseCase;
    @Autowired
    private IssueCouponQueueScheduler scheduler;
    @Autowired
    private IssuedCouponJpaRepository issuedCouponJpaRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedisKeyResolver keyResolver;

    private final long couponId = 1L;

    @BeforeEach
    void init() {
        String key = keyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_LIMIT, String.valueOf(couponId));
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        redisTemplate.opsForValue().set(key, "50");
    }

    @Test
    void 쿠폰발급_성공응답_후_스케줄러가_실행되어_쿠폰_발급에_성공하면_발급_정보가_저장된다() throws Exception {
        // given
        Long userId = 1L;

        // when
        IssueCouponUseCase.Output output = issueCouponUseCase.execute(new IssueCouponUseCase.Input(couponId, userId));
        scheduler.drain();

        // then
        assertThat(output.isSuccess()).isTrue();
        IssuedCoupon issuedCoupon =
                issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId).orElse(null);
        assertThat(issuedCoupon).isNotNull();
        assertThat(issuedCoupon.getUserId()).isEqualTo(userId);
        assertThat(issuedCoupon.getCouponId()).isEqualTo(couponId);
        assertThat(issuedCoupon.getStatus()).isEqualTo(IssuedCoupon.Status.ACTIVE);
    }

    @Test
    void 첫_요청이면_대기열에_등록된다() {
        // when
        var out = issueCouponUseCase.execute(new IssueCouponUseCase.Input(couponId, 1L));

        // then
        assertThat(out.isSuccess()).isTrue();

        String queueKey = keyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_QUEUE, String.valueOf(couponId));
        Long qsize = redisTemplate.opsForZSet().zCard(queueKey);
        assertThat(qsize).isNotNull().isEqualTo(1);

        String issueKey = keyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_USERS, String.valueOf(couponId));
        Boolean issuedMember = redisTemplate.opsForSet().isMember(issueKey, "1");
        assertThat(issuedMember).isFalse();
    }

    @Test
    void 동일사용자가_중복_요청해도_실패응답과_함께_큐크기가_증가하지_않는다() {
        // when
        var first = issueCouponUseCase.execute(new IssueCouponUseCase.Input(couponId, 1L));
        var second = issueCouponUseCase.execute(new IssueCouponUseCase.Input(couponId, 1L));

        // then
        assertThat(first.isSuccess()).isTrue();
        assertThat(second.isSuccess()).isFalse();

        String queueKey = keyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_QUEUE, String.valueOf(couponId));
        Long qsize = redisTemplate.opsForZSet().zCard(queueKey);
        assertThat(qsize).isNotNull().isEqualTo(1);
    }

    @Test
    void 이미_발급된_사용자는_실패를_응답한다() {
        // given
        String issueKey = keyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_USERS, String.valueOf(couponId));
        redisTemplate.opsForSet().add(issueKey, "1");

        // when
        var out = issueCouponUseCase.execute(new IssueCouponUseCase.Input(couponId, 1L));

        // then
        assertThat(out.isSuccess()).isFalse();

        String queueKey = keyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_QUEUE, String.valueOf(couponId));
        Long qsize = redisTemplate.opsForZSet().zCard(queueKey);
        assertThat(qsize).isNotNull().isEqualTo(0);
    }
}