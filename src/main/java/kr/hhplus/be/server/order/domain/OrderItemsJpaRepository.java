package kr.hhplus.be.server.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsJpaRepository extends JpaRepository<OrderItems, Long> {
}
