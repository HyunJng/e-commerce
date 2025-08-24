package kr.hhplus.be.server.common.lock.resolver;

import kr.hhplus.be.server.order.application.usecase.PlaceOrderUseCase;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductAndWalletLockKeyResolver implements LockKeyResolver {

    @Override
    public List<String> resolve(ProceedingJoinPoint joinPoint) {
        List<String> keys = new ArrayList<>();

        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof PlaceOrderUseCase.Input input) {
                // 상품
                input.getOrderProductIds().stream().sorted().forEach(productId -> keys.add("product:" + productId));
                // 포인트
                keys.add("wallet:user:" + input.userId());
            }
        }
        return keys;
    }
}
