package kr.hhplus.be.server.order.infrastructure;

import kr.hhplus.be.server.order.application.listener.StatisticDataPlatformSender;
import kr.hhplus.be.server.order.domain.event.PlacedOrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Deprecated
public class SpringStatisticDataPlatformSender implements StatisticDataPlatformSender {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(PlacedOrderEvent event) {
        log.info("[DATA PLATFORM] send info = {}", event);
        // 데이터 플랫폼 전송
    }
}
