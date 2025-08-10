package kr.hhplus.be.server.order.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.jpa.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "orders")
public class Order extends BaseTimeEntity {

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

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order() {
    }

    @Builder
    private Order(Long userId, Long totalAmount, Long discountAmount, Long paidAmount, Long issuedCouponId) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.paidAmount = paidAmount;
        this.issuedCouponId = issuedCouponId;
    }

    public static Order create(Long userId,
                               List<OrderItem> items,
                               Long discountAmount,
                               Long issuedCouponId) {
        Long totalAmount = items.stream()
                .mapToLong(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        Long paidAmount = totalAmount - discountAmount;

        Order order = new Order(userId, totalAmount, discountAmount, paidAmount, issuedCouponId);
        items.forEach(order::addItem);

        return order;
    }

    public void addItem(OrderItem item) {
        orderItems.add(item);
        item.assignTo(this);
    }
}
