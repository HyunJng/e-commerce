package kr.hhplus.be.server.common.event;

import lombok.Data;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaPartitionConfig {
    @Bean
    public NewTopic orderPlacedTopic(OrderCreated prop) {
        return TopicBuilder.name(prop.getName())
                .partitions(prop.getPartitions())
                .replicas(1)
                .build();
    }

    @Data
    @ConfigurationProperties(prefix = "kafka.topics.order-created")
    public static class OrderCreated {
        private String name;
        private int partitions;
    }
}
