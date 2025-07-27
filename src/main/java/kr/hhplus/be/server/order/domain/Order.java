package kr.hhplus.be.server.order.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "orders")
public class Order extends AbstractAggregateRoot<Order> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "discount_amount")
    private Long discountAmount;

    @Column(name = "paid_amount")
    private Long paidAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // TODO: 멱등키 추가하기

    public Order() {
    }

    @Builder
    public Order(Long id, Long userId, Long totalAmount, Long discountAmount, Long paidAmount, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.paidAmount = paidAmount;
        this.createdAt = createdAt;
    }

    public static Order create(Long userId, Long totalAmount, Long discountAmount, Long paidAmount, LocalDateTime now) {
        return Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .paidAmount(paidAmount)
                .createdAt(now)
                .build();
    }
}
