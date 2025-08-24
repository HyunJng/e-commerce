package kr.hhplus.be.server.large.product.infrastructure;

import kr.hhplus.be.server.common.redis.RedisKey;
import kr.hhplus.be.server.common.redis.RedisKeyResolver;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.large.AbstractConcurrencyTest;
import kr.hhplus.be.server.product.domain.entity.BestProductProperties;
import kr.hhplus.be.server.product.infrastructure.BestProductRollingAggregateScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BestProductRollingAggregateSchedulerTest extends AbstractConcurrencyTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisKeyResolver keyRegistry;
    @Autowired
    private BestProductRollingAggregateScheduler scheduler;
    @Autowired
    private DateHolder dateHolder;
    @Autowired
    private BestProductProperties properties;

    @BeforeEach
    void init() {
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    /**
     * Feature: 인기상품 집계 롤링
     *
     * Background
     *     Given agg(기존 집계)에 {1:10, 2:5} 이 존재함
     *     And new(현재시 집계)에 {2:+4, 3:+8} 이 존재함
     *     And old(72시간 전 집계)에 {1:+3, 4:+2} 이 존재함
     *
     * Scenario: 롤링 시 agg에 new를 더하고 old를 빼며 0 이하인 값은 제거함
     *
     * Then 최종 집계는 다음과 같아야 함
     *     1: 10 + 0 - 3 = 7
     *     2:  5 + 4 - 0 = 9
     *     3:  0 + 8 - 0 = 8
     *     4:  0 + 0 - 2 = -2 → 제거됨(null)
     *
     * And 정렬 순서는 점수 기준 내림차순으로 {2, 3, 1}
     */
    @Test
    void 롤링시_agg에_new를_더하고_old를_빼며_0이하_제거한다() {
        // given
        LocalDateTime now = dateHolder.now();

        String agg = RedisKey.BEST_PRODUCT_AGGREGATE.getKey();
        String newKey = keyRegistry.hourlyBucket(RedisKey.BEST_PRODUCT_RANKING, now);
        String oldKey = keyRegistry.hourlyBucket(RedisKey.BEST_PRODUCT_RANKING, now.minusDays(properties.getAggregateDays()));

        stringRedisTemplate.opsForZSet().add(agg, "1", 10.0);
        stringRedisTemplate.opsForZSet().add(agg, "2", 5.0);

        stringRedisTemplate.opsForZSet().add(newKey, "2", 4.0);
        stringRedisTemplate.opsForZSet().add(newKey, "3", 8.0);

        stringRedisTemplate.opsForZSet().add(oldKey, "1", 3.0);
        stringRedisTemplate.opsForZSet().add(oldKey, "4", 2.0);

        // when
        scheduler.rollUpBestProductRanking();

        // then
        Double s1 = stringRedisTemplate.opsForZSet().score(agg, "1");
        Double s2 = stringRedisTemplate.opsForZSet().score(agg, "2");
        Double s3 = stringRedisTemplate.opsForZSet().score(agg, "3");
        Double s4 = stringRedisTemplate.opsForZSet().score(agg, "4");

        assertThat(s1).isEqualTo(7.0);
        assertThat(s2).isEqualTo(9.0);
        assertThat(s3).isEqualTo(8.0);
        assertThat(s4).isEqualTo(null);

        var top = stringRedisTemplate.opsForZSet().reverseRangeWithScores(agg, 0, 2).stream().toList();
        assertThat(top.get(0).getValue()).isEqualTo("2");
        assertThat(top.get(1).getValue()).isEqualTo("3");
        assertThat(top.get(2).getValue()).isEqualTo("1");
    }
}