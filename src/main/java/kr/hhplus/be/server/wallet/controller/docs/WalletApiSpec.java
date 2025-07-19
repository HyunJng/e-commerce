package kr.hhplus.be.server.wallet.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.common.response.DataResponse;
import kr.hhplus.be.server.wallet.controller.dto.WalletChargeApi;
import kr.hhplus.be.server.wallet.controller.dto.WalletViewApi;

@Tag(name = "Wallet", description = "유저 지갑 관련 API")
public interface WalletApiSpec {

    @Operation(summary = "포인트 조회", description = "사용자의 포인트 정보를 조회합니다.")
    DataResponse<WalletViewApi.Response> walletView(@Parameter(description = "포인트 조회 요청") WalletViewApi.Request request);

    @Operation(summary = "포인트 충전", description = "지정한 금액만큼 포인트를 충전합니다.")
    DataResponse<WalletChargeApi.Response> walletCharge(WalletChargeApi.Request request);
}
