package kr.hhplus.be.server.order.domain.event;

import kr.hhplus.be.server.coupon.domain.entity.OrderProduct;

import java.util.List;

public record PlacedOrderEvent(List<OrderProduct> orderProducts) {
}
