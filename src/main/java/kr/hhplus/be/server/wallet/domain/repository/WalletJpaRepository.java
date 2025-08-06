package kr.hhplus.be.server.wallet.domain.repository;


import kr.hhplus.be.server.wallet.domain.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletJpaRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);
}
