package kr.hhplus.be.server.common.exception;

import io.swagger.v3.oas.annotations.Hidden;
import kr.hhplus.be.server.common.response.CommonResponse;
import kr.hhplus.be.server.common.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Hidden
@Slf4j
@RestControllerAdvice
public class GeneralExceptionAdvice {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<CommonResponse> handleCommonException(CommonException commonException) {
        return getErrorResponse(commonException.getResultCode(), commonException.getArgs());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handleException(Exception e) {
        log.error("예측하지 못한 에러 발생: {}", e.getMessage(), e);

        return getErrorResponse(ResultCode.INTERNAL_SERVER_ERROR);
    }

    private static ResponseEntity<CommonResponse> getErrorResponse(
            ResultCode resultCode,
            String... args
    ) {
        return ResponseEntity
                .status(resultCode.getStatus())
                .body(new CommonResponse(resultCode.getCode(), resultCode.getMessage(args)));
    }
}
