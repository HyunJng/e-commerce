package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.order.application.listener.StatisticDataPlatformSender;
import kr.hhplus.be.server.order.domain.event.PlacedOrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaStatisticDataPlatformConsumer implements StatisticDataPlatformSender {

    @KafkaListener(topics = "${kafka.topics.order-created.name}",
            groupId = "order-created-data-platform",
            concurrency = "${kafka.topics.order-created.partitions}")
    public void send(PlacedOrderEvent event) {
        log.info("[DATA PLATFORM] consume info = {}", event);
        // 데이터 플랫폼 전송 로직
    }
}