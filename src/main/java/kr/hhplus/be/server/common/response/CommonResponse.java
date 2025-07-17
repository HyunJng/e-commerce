package kr.hhplus.be.server.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommonResponse<T> {
    private String resultCd;
    private String resultMsg;
    private T result;

    public static <T> CommonResponse<T> success(T result) {
        return CommonResponse.<T>builder()
                .resultCd("0000")
                .resultMsg("SUCCESS")
                .result(result)
                .build();
    }
}
