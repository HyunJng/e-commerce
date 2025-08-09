package kr.hhplus.be.server.product.domain.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductLockJpaRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Product a 
            WHERE a.id IN (:productIds)
            """)
    List<Product> findAllByIdsForUpdate(List<Long> productIds);
}
