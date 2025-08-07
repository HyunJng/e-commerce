package kr.hhplus.be.server.product.domain.repository;

import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.product.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PessimisticLockException;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductLockLoader {

    private final ProductLockJpaRepository productLockJpaRepository;

    public List<Product> findAllByIds(List<Long> productIds) {
        try {
            return productLockJpaRepository.findAllByIdsForUpdate(productIds);
        } catch (PessimisticLockException | LockAcquisitionException | CannotAcquireLockException e) {
            log.error("LOCK EXCEPTION: {}", this.getClass().getName(), e);
            throw new CommonException(ErrorCode.RACE_CONDITION_EXCEPTION, e);
        }
    }
}
