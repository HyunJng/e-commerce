package kr.hhplus.be.server.medium;

import kr.hhplus.be.server.config.TestBeanConfiguration;
import kr.hhplus.be.server.config.TestLogConfiguration;
import kr.hhplus.be.server.config.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TestBeanConfiguration.class, TestcontainersConfiguration.class, TestLogConfiguration.class})
public abstract class AbstractIntegrationTest {
}
