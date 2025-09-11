package kr.hhplus.be.server.common.dummy;

import kr.hhplus.be.server.product.infrastructure.BestProductRollingAggregateScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class BestProductSchedulerStarter {
    private final BestProductRollingAggregateScheduler rollup;

    @Bean
    public ApplicationRunner bestRollupOnStart() {
        return args -> rollup.rollUpBestProductRanking();
    }
}
