package kr.hhplus.be.server.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "오류 응답 포맷")
public class ErrorResponse {

    @Schema(description = "응답 코드")
    private final String resultCd;

    @Schema(description = "응답 메시지")
    private final String resultMsg;

    @Builder
    public ErrorResponse(String resultCd, String resultMsg) {
        this.resultCd = resultCd;
        this.resultMsg = resultMsg;
    }
}
