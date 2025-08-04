package kr.hhplus.be.server.config;

import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.mock.MockDateHolderImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Month;

@TestConfiguration
public class TestBeanConfiguration {

    @Bean
    public DateHolder dateHolder() {
        return new MockDateHolderImpl(2025, Month.JULY, 31, 0, 0);
    }

}