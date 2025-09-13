package kr.hhplus.be.server.common.dummy;

import kr.hhplus.be.server.common.redis.RedisKey;
import kr.hhplus.be.server.common.redis.RedisKeyResolver;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.product.domain.entity.BestProductProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Profile("local")
public class BestProductRedisDummy implements CommandLineRunner {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisKeyResolver redisKeyResolver;
    private final DateHolder dateHolder;
    private final BestProductProperties properties;

    @Override
    public void run(String... args) {
        String key = redisKeyResolver.hourlyBucket(RedisKey.BEST_PRODUCT_RANKING, dateHolder.now());

        for (long productId = 1; productId <= 10; productId++) {
            double score = Math.random() * 100; // 임의 구매 수량
            stringRedisTemplate.opsForZSet()
                    .add(key, String.valueOf(productId), score);
        }

        stringRedisTemplate.expire(key, Duration.ofDays(properties.getAggregateDays()));
    }
}