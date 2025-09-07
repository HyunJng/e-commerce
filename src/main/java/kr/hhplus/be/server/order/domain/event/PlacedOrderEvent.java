package kr.hhplus.be.server.order.domain.event;

import kr.hhplus.be.server.coupon.domain.entity.OrderProduct;
import lombok.Getter;

import java.util.List;

@Getter
public class PlacedOrderEvent {

    private List<OrderProduct> orderProducts;

    public PlacedOrderEvent() {
    }

    public PlacedOrderEvent(List<OrderProduct> orderProducts) {
        this.orderProducts = orderProducts;
    }
}
