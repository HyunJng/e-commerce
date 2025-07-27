package kr.hhplus.be.server.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "데이터 응답 포맷")
public class DataResponse<T> extends CommonResponse {

    @Schema(description = "응답 데이터")
    private T result;

    private DataResponse(T result) {
        super(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
        this.result = result;
    }

    public static <T> DataResponse<T> success(T result) {
        return new DataResponse<>(result);
    }

}
