package kr.hhplus.be.server.large.product;

import kr.hhplus.be.server.comparison.FindBestProductUseCaseForNotCache;
import kr.hhplus.be.server.large.AbstractConcurrencyTest;
import kr.hhplus.be.server.product.application.usecase.FindBestProductsUseCase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/best-product-throughput-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
@Disabled
public class BestProductThroughputTest extends AbstractConcurrencyTest {

    @Autowired
    private FindBestProductsUseCase findBestProductsUseCase;
    @Autowired
    private FindBestProductUseCaseForNotCache findBestProductUseCaseForNotCache;


    long start;

    @BeforeEach
    void startTimer() {
        start = System.nanoTime();
    }

    @AfterEach
    void endTimer(TestInfo testInfo) {
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        System.out.println(testInfo.getDisplayName() + " 실행 시간: " + elapsed + "ms");
    }

    @Test
    void 인기상품조회_API에_캐시를_적용했을_때의_성능을_확인한다() throws Exception {
        // when
        runConcurrentTest(8, 5000, i -> {
            findBestProductsUseCase.execute();
        });
    }

//    @Test
    void 인기상품조회_API에_캐시를_적용하지_않았을_때의_성능을_확인한다() throws Exception {
        // when
        runConcurrentTest(8, 5000, i -> {
            findBestProductUseCaseForNotCache.execute();
        });
    }
}
