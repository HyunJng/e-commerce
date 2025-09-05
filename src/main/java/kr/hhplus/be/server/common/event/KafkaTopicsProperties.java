package kr.hhplus.be.server.common.event;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "kafka.topics")
public class KafkaTopicsProperties {
    private TopicProperties orderCreated;

    @Data
    public static class TopicProperties {
        private String name;
        private Integer partitions;
        private Integer replicas;
    }
}
