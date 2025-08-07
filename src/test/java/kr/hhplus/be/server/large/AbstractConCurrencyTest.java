package kr.hhplus.be.server.large;

import kr.hhplus.be.server.config.TestBeanConfiguration;
import kr.hhplus.be.server.config.TestLogConfiguration;
import kr.hhplus.be.server.config.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestBeanConfiguration.class, TestcontainersConfiguration.class, TestLogConfiguration.class})
public abstract class AbstractConCurrencyTest {

    public static void runConcurrentTest(int threadCount, Consumer<Integer> consumer) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    consumer.accept(finalI);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    countDownLatch.countDown();
                }
            });

        }

        startLatch.countDown();
        countDownLatch.await();
    }
}
