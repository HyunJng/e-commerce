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

    public String argumentsBucket(RedisKey key, String... args) {
        String result = key.getKey();
        if (args == null || args.length == 0 || result == null || !result.contains("%")) {
            return result;
        }

        for (int i = 0; i < args.length; i++) {
            result = result.replaceAll("%" + (i + 1), args[i]);
        }

        return result;
    }
}
