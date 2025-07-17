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
public class CommonResponse<T> {
    @Schema(description = "응답 코드", example = "0000")
    private String resultCd;

    @Schema(description = "응답 메시지", example = "SUCCESS")
    private String resultMsg;

    @Schema(description = "응답 데이터")
    private T result;

    public static <T> CommonResponse<T> success(T result) {
        return CommonResponse.<T>builder()
                .resultCd("0000")
                .resultMsg("SUCCESS")
                .result(result)
                .build();
    }
}
