package kr.hhplus.be.server.product.domain;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Long price;

    @Column(name = "stock_quantity")
    private Integer quantity;

    @Column(name = "create_at", insertable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at", insertable = false, updatable = false)
    private LocalDateTime updateAt;

    public Product() {}

    public Product(Long id, String name, Long price, Integer quantity, LocalDateTime createAt, LocalDateTime updateAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public void decreaseQuantity(Integer quantity) {
        if (this.quantity < quantity) {
            throw new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "상품 재고");
        }
        this.quantity -= quantity;
    }
}
