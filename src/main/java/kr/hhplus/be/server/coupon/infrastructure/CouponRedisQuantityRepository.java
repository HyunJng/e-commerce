package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.common.redis.RedisKey;
import kr.hhplus.be.server.common.redis.RedisKeyResolver;
import kr.hhplus.be.server.coupon.application.port.CouponQuantityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CouponRedisQuantityRepository implements CouponQuantityRepository {

    private final StringRedisTemplate redisTemplate;
    private final RedisKeyResolver redisKeyResolver;

    public Long getLimit(Long couponId) {
        String key = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_LIMIT, String.valueOf(couponId));
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0L : Long.parseLong(value);
    }

    public Long getIssuedUsersCount(Long couponId) {
        String key = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_USERS, String.valueOf(couponId));
        Long size = redisTemplate.opsForSet().size(key);
        return size == null ? 0L : size;
    }

    public boolean isAlreadyIssued(Long couponId, Long userId) {
        String key = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_USERS, String.valueOf(couponId));
        Boolean b = redisTemplate.opsForSet().isMember(key, String.valueOf(userId));
        return Boolean.TRUE.equals(b);
    }

    public boolean enqueue(long couponId, long userId, long nowMillis) {
        String key = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_QUEUE, String.valueOf(couponId));
        Boolean added = redisTemplate.opsForZSet().add(key, String.valueOf(userId), nowMillis);
        return Boolean.TRUE.equals(added);
    }

    public List<Long> popNext(long couponId, int n) {
        String key = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_QUEUE, String.valueOf(couponId));
        Set<ZSetOperations.TypedTuple<String>> popped = redisTemplate.opsForZSet().popMin(key, n);
        if (popped == null || popped.isEmpty()) return List.of();
        return popped.stream().map(ZSetOperations.TypedTuple::getValue).map(Long::valueOf).toList();
    }

    public void markIssued(long couponId, long userId) {
        String key = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_USERS, String.valueOf(couponId));
        redisTemplate.opsForSet().add(key, String.valueOf(userId));
    }
}
