package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.common.redis.RedisKey;
import kr.hhplus.be.server.common.redis.RedisKeyResolver;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.product.application.port.BestProductRankingAggregateWriter;
import kr.hhplus.be.server.product.domain.entity.BestProductProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisBestProductRankingAggregateWriter implements BestProductRankingAggregateWriter {

    private final RedisKeyResolver redisKeyResolver;
    private final StringRedisTemplate stringRedisTemplate;
    private final DateHolder dateHolder;
    private final BestProductProperties properties;

    @Override
    public void incrementBestProductRanking(Long productId, Integer quantity) {
        String key = redisKeyResolver.hourlyBucket(RedisKey.BEST_PRODUCT_RANKING, dateHolder.now());
        stringRedisTemplate
                .opsForZSet()
                .incrementScore(key, productId.toString(), quantity);

        stringRedisTemplate.expire(key, Duration.ofDays(properties.getAggregateDays()));
    }
}
