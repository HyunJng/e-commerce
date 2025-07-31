package kr.hhplus.be.server.order.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.time.DateHolder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "order_items")
public class OrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price")
    private Long unitPrice;

    @Column(name = "total_price")
    private Long totalPrice;

    @Column(name = "reg_date")
    private LocalDate regDate;

    public OrderItems() {
    }

    @Builder
    public OrderItems(Long id, Order order, Long productId, Integer quantity, Long unitPrice, Long totalPrice, LocalDate regDate) {
        this.id = id;
        this.order = order;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.regDate = regDate;
    }

    public static OrderItems create(Order order,
                                    Long productId,
                                    Integer quantity,
                                    Long unitPrice,
                                    DateHolder dateHolder) {
        Long totalPrice = unitPrice * quantity;
        return OrderItems.builder()
                .order(order)
                .productId(productId)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .regDate(dateHolder.today())
                .build();
    }

}