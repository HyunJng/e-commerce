package kr.hhplus.be.server.large.common.lock;

import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class LockTxProbeConfig {

    @Bean
    public LockTxProbeAspect lockTxProbeAspect(
            RedissonClient redissonClient,
            ApplicationContext applicationContext
    ){
        return new LockTxProbeAspect(redissonClient, applicationContext);
    }
}
