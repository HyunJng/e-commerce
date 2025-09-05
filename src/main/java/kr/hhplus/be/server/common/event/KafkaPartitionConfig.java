package kr.hhplus.be.server.common.event;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaPartitionConfig {
    @Bean
    public NewTopic orderPlacedTopic(KafkaTopicsProperties prop) {
        return TopicBuilder.name(prop.getOrderCreated().getName())
                .partitions(prop.getOrderCreated().getPartitions())
                .replicas(prop.getOrderCreated().getReplicas())
                .build();
    }
}
