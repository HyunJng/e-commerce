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

    @Column(name = "issued_coupon_id")
    private Long issuedCouponId;

    @Column(name = "paid_amount")
    private Long paidAmount;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    public Order() {
    }

    @Builder
    public Order(Long id, Long userId, Long totalAmount, Long discountAmount, Long issuedCouponId, Long paidAmount, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.issuedCouponId = issuedCouponId;
        this.paidAmount = paidAmount;
        this.createAt = createdAt;
    }

    public static Order create(Long userId,
                               Long totalAmount,
                               Long discountAmount,
                               Long paidAmount,
                               Long issuedCouponId) {
        return Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .paidAmount(paidAmount)
                .issuedCouponId(issuedCouponId)
                .build();
    }
}
