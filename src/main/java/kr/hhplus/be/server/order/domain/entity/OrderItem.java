package kr.hhplus.be.server.order.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.jpa.BaseTimeEntity;
import kr.hhplus.be.server.common.time.DateHolder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "unit_price")
    private Long unitPrice;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "reg_date")
    private LocalDate regDate;

    public OrderItem() {
    }

    private OrderItem(Long productId, Integer quantity, Long unitPrice, LocalDate regDate) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.regDate = regDate;
    }

    public static OrderItem of(Long productId,
                               Integer quantity,
                               Long unitPrice,
                               DateHolder dateHolder) {
        return new OrderItem(productId, quantity, unitPrice, dateHolder.today());
    }

    public Long totalAmount() {
        return this.unitPrice * this.quantity;
    }

    public void assignTo(Order order) {
        this.order = order;
    }
}