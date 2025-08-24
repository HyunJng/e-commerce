package kr.hhplus.be.server.large.product.infrastructure;


import kr.hhplus.be.server.common.redis.RedisKey;
import kr.hhplus.be.server.common.redis.RedisKeyResolver;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.large.AbstractConcurrencyTest;
import kr.hhplus.be.server.product.domain.entity.BestProduct;
import kr.hhplus.be.server.product.infrastructure.RedisBestProductRankingReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/best-product-ranking-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
class RedisBestProductRankingReaderTest extends AbstractConcurrencyTest {

    @Autowired
    private RedisKeyResolver redisKeyResolver;
    @Autowired
    private DateHolder dateHolder;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisBestProductRankingReader reader;

    @BeforeEach
    void init() {
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }

    /**
     * Feature: 인기상품 집계 조회
     *
     * Background
     *     Given 기존 집계에 다음과 같은 점수가 있음
     *         [1:100, 2:90, 3:80, 4:70, 5:60, 6:50]
     *     And 현재 집계에 다음과 같은 점수가 추가됨
     *         [6:+30, 10:+120, 11:+65]
     *
     * Scenario: 기존 집계와 현재 집계를 합산하여 상위 5개 상품을 조회함
     *
     * Then 최종 상위 5개 상품은 {10,1,2,3,6} 또는 {10,1,2,6,3} 이어야 함
     *     10: 0+120 = 120
     *     1: 100+0 = 100
     *     2:  90+0 =  90
     *     3:  80+0 =  80
     *     6:  50+30 =  80 (동점 → ID 순서는 구현마다 다를 수 있음)
     *     11: 0+65 =  65 → 탈락
     */
    @Test
    void 인기상품_집계와_현재집계를_합산하여_상위5개를_조회한다() throws Exception {
        // given
        String agg = RedisKey.BEST_PRODUCT_AGGREGATE.getKey();
        String curr = redisKeyResolver.hourlyBucket(RedisKey.BEST_PRODUCT_RANKING, dateHolder.now());

        stringRedisTemplate.opsForZSet().add(agg, "1", 100.0);
        stringRedisTemplate.opsForZSet().add(agg, "2", 90.0);
        stringRedisTemplate.opsForZSet().add(agg, "3", 80.0);
        stringRedisTemplate.opsForZSet().add(agg, "4", 70.0);
        stringRedisTemplate.opsForZSet().add(agg, "5", 60.0);
        stringRedisTemplate.opsForZSet().add(agg, "6", 50.0);

        stringRedisTemplate.opsForZSet().add(curr, "6", 30.0);
        stringRedisTemplate.opsForZSet().add(curr, "10", 120.0);
        stringRedisTemplate.opsForZSet().add(curr, "11", 65.0);

        // when
        List<BestProduct> top = reader.get();

        // then
        List<Long> ids = top.stream().map(BestProduct::id).toList();

        assertThat(ids).contains(10L, 1L, 2L, 3L, 6L);
        assertThat(ids.size()).isEqualTo(5);
        assertThat(top.get(0).name()).isNotBlank();
    }
}