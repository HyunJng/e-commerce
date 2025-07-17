package kr.hhplus.be.server.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "공통 응답 포맷")
public class CommonResponse {
    @Schema(description = "응답 코드", example = "0000")
    private String resultCd;

    @Schema(description = "응답 메시지", example = "SUCCESS")
    private String resultMsg;
}
