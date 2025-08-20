package kr.hhplus.be.server.large.product.infrastructure;

import kr.hhplus.be.server.common.redis.RedisKey;
import kr.hhplus.be.server.common.redis.RedisKeyResolver;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.large.AbstractConcurrencyTest;
import kr.hhplus.be.server.product.infrastructure.RedisBestProductRankingAggregateWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static org.assertj.core.api.Assertions.assertThat;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/best-product-ranking-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
class RedisBestProductRankingAggregateWriterTest extends AbstractConcurrencyTest {

    @Autowired
    private RedisBestProductRankingAggregateWriter redisBestProductRankingWriter;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedisKeyResolver redisKeyResolver;
    @Autowired
    private DateHolder dateHolder;

    private static String BEST_PRODUCT_RANKING_KEY;

    @BeforeEach
    void init() {
        BEST_PRODUCT_RANKING_KEY = redisKeyResolver.hourlyBucket(RedisKey.BEST_PRODUCT_RANKING, dateHolder.now());
        redisTemplate.delete(BEST_PRODUCT_RANKING_KEY);
    }

    @Test
    void redis에_상품이_존재하면_주문수량만큼_score를_더한다() throws Exception {
        // given
        Long productId = 1L;
        redisTemplate.opsForZSet().add(BEST_PRODUCT_RANKING_KEY, productId.toString(), 10);

        // when
        redisBestProductRankingWriter.incrementBestProductRanking(productId, 5);

        // then
        Double score = redisTemplate.opsForZSet().score(BEST_PRODUCT_RANKING_KEY, String.valueOf(productId));
        assertThat(score).isEqualTo(15);
    }

}