package kr.hhplus.be.server.mock;

import kr.hhplus.be.server.common.time.DateHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

public class MockDateHolderImpl implements DateHolder {

    private LocalDateTime today;

    public MockDateHolderImpl(int year, Month month, int dayOfMonth, int hour, int minute) {
        today = LocalDateTime.of(year, month, dayOfMonth, hour, minute, 0);
    }

    @Override
    public LocalDate today() {
        return today.toLocalDate();
    }

    @Override
    public LocalDateTime now() {
        return today;
    }
}
