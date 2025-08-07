package kr.hhplus.be.server.order.domain.repository;

import kr.hhplus.be.server.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

}
