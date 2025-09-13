package kr.hhplus.be.server.product.infrastructure;

import kr.hhplus.be.server.common.redis.RedisKey;
import kr.hhplus.be.server.common.redis.RedisKeyResolver;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.product.domain.entity.BestProductProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.zset.Aggregate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BestProductRollingAggregateScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisKeyResolver redisKeyResolver;
    private final DateHolder dateHolder;
    private final BestProductProperties properties;

    @Scheduled(cron = "0 0 * * * *")
    public void rollUpBestProductRanking() {
        String aggKey = RedisKey.BEST_PRODUCT_AGGREGATE.getKey();
        String tempKey = RedisKey.BEST_PRODUCT_AGGREGATE_TEMP.getKey();
        String newKey = redisKeyResolver.hourlyBucket(RedisKey.BEST_PRODUCT_RANKING, dateHolder.now());
        String oldKey = redisKeyResolver.hourlyBucket(RedisKey.BEST_PRODUCT_RANKING, dateHolder.now().minusDays(3));

        // tmp = agg(+1) + new(+1) + old(-1)
        stringRedisTemplate.execute((RedisCallback<? extends Object>) connection -> {
            byte[] dst = tempKey.getBytes();
            byte[][] src = new byte[][]{
                    aggKey.getBytes(),
                    newKey.getBytes(),
                    oldKey.getBytes()
            };
            int[] weights = new int[]{1, 1, -1};
            connection.zUnionStore(dst, Aggregate.SUM, weights, src);
            return null;
        });

        // 점수 0 이하 제거
        stringRedisTemplate
                .opsForZSet()
                .removeRangeByScore(tempKey, Double.NEGATIVE_INFINITY, 0.0d);

        // TOP-K만 유지 (낮은 점수부터 제거)
        Long size = stringRedisTemplate.opsForZSet().size(tempKey);
        if (size != null && size > properties.getAggregatePastCandidate()) {
            long toRemoveEndRank = size - properties.getAggregatePastCandidate() - 1L;
            if (toRemoveEndRank >= 0L) {
                stringRedisTemplate.opsForZSet().removeRange(tempKey, 0L, toRemoveEndRank);
            }
        }

        // aggKey에 tempKey 덮어쓰기
        Boolean hasTemp = stringRedisTemplate.hasKey(tempKey);
        if (Boolean.TRUE.equals(hasTemp)) {
            stringRedisTemplate.rename(tempKey, aggKey);
        } else {
            stringRedisTemplate.delete(aggKey);
        }
    }
}
