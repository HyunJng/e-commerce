package kr.hhplus.be.server.large.coupon;

import kr.hhplus.be.server.common.redis.RedisKey;
import kr.hhplus.be.server.common.redis.RedisKeyResolver;
import kr.hhplus.be.server.coupon.application.service.CouponIssueProcessor;
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
public class IssueCouponConcurrencyTest extends AbstractConcurrencyTest {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private IssueCouponQueueScheduler scheduler;
    @Autowired
    private RedisKeyResolver redisKeyResolver;
    @Autowired
    private CouponIssueProcessor processor;

    private final Long couponId = 1L;

    @BeforeEach
    void init() {
        String limitKey = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_LIMIT, String.valueOf(couponId));
        String queueKey = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_QUEUE, String.valueOf(couponId));

        redisTemplate.getConnectionFactory().getConnection().flushAll();

        redisTemplate.opsForValue().set(limitKey, "50");

        long now = System.currentTimeMillis();
        for (long userId = 1; userId <= 100; userId++) {
            redisTemplate.opsForZSet().add(queueKey, String.valueOf(userId), now + userId);
        }
    }


    /**
     * Feature: 쿠폰 발급 스케줄러
     *
     * Background
     *     Given 쿠폰 발급 한도(limit)가 50으로 설정됨
     *     And 대기열(queue)에 100명의 사용자가 등록되어 있음
     *
     * Scenario: 스케줄러가 한 번 실행될 때
     *     When 스케줄러가 실행되면
     *     Then 최대 한도인 50명까지만 발급됨
     *     And issued SET 크기는 50이어야 함
     *     And queue 잔여 인원은 50이어야 함
     */
    @Test
    void 스케줄러_1회_실행하면_한도까지만_발급한다() {
        // given
        String queueKey = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_QUEUE, String.valueOf(couponId));
        String issueKey = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_USERS, String.valueOf(couponId));

        // when
        scheduler.drain();

        // then
        long issued = size(redisTemplate.opsForSet().size(issueKey));
        long q = zcard(redisTemplate, queueKey);

        assertThat(issued).isEqualTo(50);
        assertThat(q).isEqualTo(50);
    }

    /**
     * Feature: 분산락을 통한 동시 발급 제어
     *
     * Background
     *     Given 쿠폰 발급 한도(limit)가 50으로 설정됨
     *     And 대기열(queue)에 100명의 사용자가 등록되어 있음
     *
     * Scenario: 동일한 쿠폰에 대해 drain 메서드가 동시에 2회 실행될 때
     *     When 두 쓰레드가 동시에 drain을 호출하면
     *     Then 분산락 덕분에 단일 워커만 발급을 처리함
     *     And 최종 issued SET 크기는 50이어야 함
     */
    @Test
    void 분산락이_동작하여_동시에_drain_두번_호출해도_총량_초과하지_않는다() throws Exception {
        // when
        runConcurrentTest(2, 2, i -> {
            processor.drain(couponId, 100);
        });

        // then: issued ≤ 30, queue 줄었고, DB save도 30회까지만
        String key = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_USERS, String.valueOf(couponId));
        long issued = size(redisTemplate.opsForSet().size(key));
        assertThat(issued).isEqualTo(50);
    }

    private long size(Long v) { return v == null ? 0L : v; }

    private long zcard(StringRedisTemplate redis, String key) {
        Long v = redis.opsForZSet().zCard(key);
        return v == null ? 0L : v;
    }

}
