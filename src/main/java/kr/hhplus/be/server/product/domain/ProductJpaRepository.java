package kr.hhplus.be.server.product.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    // TODO: best 상품 조회 쿼리 작성하기
    @Query("SELECT p FROM Product p")
    List<Product> findBestProducts();
}
