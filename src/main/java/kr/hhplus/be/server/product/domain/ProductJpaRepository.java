package kr.hhplus.be.server.product.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Query("""
            SELECT b
            FROM OrderItems a
            JOIN Product b ON a.productId = b.id
            WHERE a.regDate BETWEEN :startDate AND :endDate
            GROUP BY b.id
            ORDER BY COUNT(a) DESC
            """)
    List<Product> findBestProductsBetweenDays(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable);
}