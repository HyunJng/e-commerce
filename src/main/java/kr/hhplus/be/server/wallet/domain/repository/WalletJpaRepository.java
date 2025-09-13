package kr.hhplus.be.server.wallet.domain.repository;


import kr.hhplus.be.server.wallet.domain.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletJpaRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    @Modifying
    @Query("""
            UPDATE Wallet w
            SET w.balance = w.balance - :payAmount
            WHERE w.id = :id AND w.balance >= :payAmount
            """)
    int pay(Long id, Long payAmount);
}
