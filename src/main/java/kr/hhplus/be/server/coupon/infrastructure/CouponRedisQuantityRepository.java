package kr.hhplus.be.server.coupon.infrastructure;

import kr.hhplus.be.server.common.redis.RedisKey;
import kr.hhplus.be.server.common.redis.RedisKeyResolver;
import kr.hhplus.be.server.coupon.application.port.CouponQuantityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

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

    public void markIssued(long couponId, long userId) {
        String key = redisKeyResolver.argumentsBucket(RedisKey.COUPON_ISSUED_USERS, String.valueOf(couponId));
        redisTemplate.opsForSet().add(key, String.valueOf(userId));
    }
}
