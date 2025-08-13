package kr.hhplus.be.server.common.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConditionalOnProperty(
        name = "app.scheduling.enabled", havingValue = "true"
)
@Configuration
@EnableScheduling
public class SchedulerConfig {
}
