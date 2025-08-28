package kr.hhplus.be.server.product.application.listener;

import kr.hhplus.be.server.order.domain.event.PlacedOrderEvent;

public interface BestProductRankingAggregateHandler {
    void handle(PlacedOrderEvent event);
}
