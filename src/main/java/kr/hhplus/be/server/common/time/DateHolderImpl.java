package kr.hhplus.be.server.common.time;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DateHolderImpl implements DateHolder {

    @Override
    public LocalDate today() {
        return LocalDate.now();
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
