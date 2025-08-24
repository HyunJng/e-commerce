package kr.hhplus.be.server.product.application.listener;

import kr.hhplus.be.server.order.domain.event.PlacedOrderEvent;
import kr.hhplus.be.server.product.application.port.BestProductRankingAggregateWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BestProductRankingAggregateHandler {

    private final BestProductRankingAggregateWriter bestProductRankingAggregateWriter;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handler(PlacedOrderEvent event) {
        event.orderProducts().forEach(orderItem -> {
            Long productId = orderItem.productId();
            Integer quantity = orderItem.quantity();
            bestProductRankingAggregateWriter.incrementBestProductRanking(productId, quantity);
        });
    }
}
