package kr.hhplus.be.server.mock;

import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.wallet.domain.Wallet;

public class DomainTestFixtures {

    public static Coupon 기본쿠폰() {
        return new Coupon(
                1L,
                "회원가입쿠폰",
                10L,
                Coupon.DiscountType.PERCENT,
                7,
                null
        );
    }

    public static Product 기본상품() {
        return new Product(1L, "상품1", 1000L, 10, null, null);
    }

    public static Product 포맷상품(Long id) {
        return new Product(id, "상품" + id, id * 1000, (int) (10 * id), null, null);
    }

    public static Wallet 기본지갑() {
        return new Wallet(
                1L,
                1000L
        );
    }
}
