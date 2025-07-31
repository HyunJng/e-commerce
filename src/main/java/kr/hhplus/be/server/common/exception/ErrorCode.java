package kr.hhplus.be.server.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "E001", "유효하지 않은 요청(%1)"),
    NOT_FOUND_RESOURCE(HttpStatus.BAD_REQUEST, "E404", "존재하지 않는 %1"),
    INVALID_POLICY(HttpStatus.BAD_REQUEST, "E405", "정책 위반(%1)"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E999", "미지정 오류(관리자 연락 필요)");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public String getMessage(String... args) {
        String result = message;
        if (args == null || args.length == 0 || message == null || !message.contains("%")) {
            return message;
        }

        for (int i = 0; i < args.length; i++) {
            result = result.replaceAll("%" + (i + 1), args[i]);
        }

        return result;
    }
}
