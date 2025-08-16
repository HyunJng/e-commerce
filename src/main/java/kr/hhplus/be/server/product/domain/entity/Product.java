package kr.hhplus.be.server.product.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.jpa.BaseTimeEntity;
import lombok.Getter;

@Getter
@Entity
@Table(name = "products")
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "price")
    private Long price;

    @Column(name = "stock_quantity")
    private Integer quantity;

    public Product() {}

    public Product(Long id, String name, Long price, Integer quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Long calculateAmount(Integer quantity) {
        return this.price * quantity;
    }

    public void decreaseQuantity(Integer quantity) {
        if (this.quantity < quantity) {
            throw new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "상품 재고");
        }
        this.quantity -= quantity;
    }
}
