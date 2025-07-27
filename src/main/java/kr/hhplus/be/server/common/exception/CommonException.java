package kr.hhplus.be.server.common.exception;

import kr.hhplus.be.server.common.response.ResultCode;
import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {
    private final ResultCode resultCode;
    private final String[] args;

    public CommonException(ResultCode resultCode, String... args) {
        super(resultCode.getMessage(args));
        this.resultCode = resultCode;
        this.args = args;
    }
}
