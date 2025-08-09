package kr.hhplus.be.server.large;

import kr.hhplus.be.server.config.TestBeanConfiguration;
import kr.hhplus.be.server.config.TestLogConfiguration;
import kr.hhplus.be.server.config.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestBeanConfiguration.class, TestcontainersConfiguration.class, TestLogConfiguration.class})
public abstract class AbstractConCurrencyTest {

    public static int runConcurrentTest(int threadCount, Consumer<Integer> consumer) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    consumer.accept(finalI);
                    successCount.getAndIncrement();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                } finally {
                    countDownLatch.countDown();
                }
            });

        }

        startLatch.countDown();
        countDownLatch.await();
        return successCount.get();
    }

    public static List<Boolean> runConcurrentTest(int threadCount, Function<Integer, Boolean> task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    results.add(task.apply(finalI));
                } catch (Exception e) {
                    results.add(false);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        countDownLatch.await();
        return results;
    }
}
