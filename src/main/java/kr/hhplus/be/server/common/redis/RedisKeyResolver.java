package kr.hhplus.be.server.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class RedisKeyResolver {

    public String hourlyBucket(RedisKey key, LocalDateTime hour) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHH");
        return key.getKey() + ":" + hour.format(fmt);
    }
}
