package kr.hhplus.be.server.large.order;

import kr.hhplus.be.server.large.AbstractConcurrencyTest;
import kr.hhplus.be.server.large.common.lock.LockTxProbeConfig;
import kr.hhplus.be.server.order.application.usecase.PlaceOrderUseCase;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductJpaRepository;
import kr.hhplus.be.server.wallet.domain.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.repository.WalletJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/order-concurrency-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
@Import(value = {LockTxProbeConfig.class})
public class PlaceOrderUseCaseConcurrencyTest extends AbstractConcurrencyTest {

    @Autowired
    private PlaceOrderUseCase placeOrderUseCase;

    @Autowired
    private ProductJpaRepository productJpaRepository;
    @Autowired
    private WalletJpaRepository walletJpaRepository;

    @Test
    void 재고가_1개일_때_동시에_주문이_들어와도_1명만_성공한다() throws Exception {
        // given
        long productId = 1L;

        // when
        int successCount = runConcurrentTest(2, i -> {
            Long userId = (long) (i + 1);
            PlaceOrderUseCase.Input input = new PlaceOrderUseCase.Input(
                    userId,
                    null,
                    List.of(new PlaceOrderUseCase.Input.OrderProduct(productId, 1)));
            placeOrderUseCase.execute(input);
        });

        // then
        Product product = productJpaRepository.findById(productId).orElse(null);
        assertThat(product).isNotNull();
        assertThat(product.getQuantity()).isEqualTo(0);
        assertThat(successCount).isEqualTo(1);
    }

    @Test
    void 재고가_충분할_때_두_명이_동시에_주문해도_모두_성공한다() throws Exception {
        // given
        long productId = 2L;

        // when
        int successCount = runConcurrentTest(2, i -> {
            Long userId = (long) (i + 1);
            PlaceOrderUseCase.Input input = new PlaceOrderUseCase.Input(
                    userId,
                    null,
                    List.of(new PlaceOrderUseCase.Input.OrderProduct(productId, 1)));
            placeOrderUseCase.execute(input);
        });

        // then
        assertThat(successCount).isEqualTo(2);
    }

    @Test
    void 동일한_사용자가_동시에_두_번_요청을_보내면_순차처리된다() throws Exception {
        // given
        long userId = 1L, productId = 2L;

        // when
        int successCount = runConcurrentTest(2, i -> {
            PlaceOrderUseCase.Input input = new PlaceOrderUseCase.Input(
                    userId,
                    null,
                    List.of(new PlaceOrderUseCase.Input.OrderProduct(productId, 1)));
            placeOrderUseCase.execute(input);
        });

        // then
        Wallet wallet = walletJpaRepository.findByUserId(userId).get();
        Product product = productJpaRepository.findById(productId).get();
        assertThat(successCount).isEqualTo(2);
        assertThat(wallet.getBalance()).isEqualTo(96000); // 100000 - 2000 * 2
        assertThat(product.getQuantity()).isEqualTo(3); // 5 - 2
    }

    @Test
    void 동일한_쿠폰을_중복_사용_요청을_동시에_보내면_하나의_거래만_성공한다() throws Exception {
        // given
        long userId = 1L, productId = 2L, couponId = 1L;

        // when
        int successCount = runConcurrentTest(2, i -> {
            PlaceOrderUseCase.Input input = new PlaceOrderUseCase.Input(
                    userId,
                    couponId,
                    List.of(new PlaceOrderUseCase.Input.OrderProduct((long) (i + 1), 1)));
            placeOrderUseCase.execute(input);
        });

        // then
        assertThat(successCount).isEqualTo(1);
    }
}
