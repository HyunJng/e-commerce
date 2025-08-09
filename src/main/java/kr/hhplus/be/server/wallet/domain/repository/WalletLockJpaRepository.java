package kr.hhplus.be.server.wallet.domain.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.wallet.domain.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletLockJpaRepository extends JpaRepository<Wallet, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Wallet a
            WHERE a.userId = :userId
            """)
    Optional<Wallet> findByUserIdForUpdate(Long userId);

}
