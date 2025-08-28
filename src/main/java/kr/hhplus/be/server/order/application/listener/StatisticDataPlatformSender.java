package kr.hhplus.be.server.order.application.listener;

import kr.hhplus.be.server.order.domain.event.PlacedOrderEvent;

public interface StatisticDataPlatformSender {
    void send(PlacedOrderEvent event);
}