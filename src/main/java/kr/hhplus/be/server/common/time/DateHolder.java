package kr.hhplus.be.server.common.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DateHolder {

    LocalDate today();

    LocalDateTime now();
}
