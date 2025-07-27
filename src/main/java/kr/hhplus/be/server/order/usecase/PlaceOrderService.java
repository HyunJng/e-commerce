package kr.hhplus.be.server.order.usecase;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.domain.Coupon;
import kr.hhplus.be.server.coupon.domain.CouponJpaRepository;
import kr.hhplus.be.server.coupon.domain.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.IssuedCouponJpaRepository;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.OrderJpaRepository;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductJpaRepository;
import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceOrderService {

    public record Input(
            Long userId,
            Long couponId,
            List<OrderProduct> orderProduct
    ){
        public record OrderProduct(
                Long productId,
                Integer quantity
        ) {
        }
    }

    public record Output(
            Long orderId,
            Long userId,
            Long totalAmount,
            Long discountAmount,
            Long paidAmount,
            LocalDateTime createAt
    ) {
        public static Output from(Order order) {
            return new Output(
                    order.getId(),
                    order.getUserId(),
                    order.getTotalAmount(),
                    order.getDiscountAmount(),
                    order.getPaidAmount(),
                    order.getCreatedAt()
            );
        }
    }

    private final OrderJpaRepository orderJpaRepository;
    private final ProductJpaRepository productJpaRepository;
    private final CouponJpaRepository couponJpaRepository;
    private final IssuedCouponJpaRepository issuedCouponJpaRepository;
    private final WalletJpaRepository walletJpaRepository;
    private final DateHolder dateHolder;

    @Transactional
    public Output execute(Input input) {
        List<Product> products = productJpaRepository.findAllById(
                input.orderProduct.stream().map(Input.OrderProduct::productId).toList()
        );
        validateProducts(products, input.orderProduct);

        Long totalAmount = calculateTotalAmount(products, input.orderProduct);

        Coupon coupon = null;
        IssuedCoupon issuedCoupon = null;
        Long discountAmount = 0L;
        if (input.couponId != null) {
            coupon = couponJpaRepository.findById(input.couponId)
                    .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "쿠폰"));
            issuedCoupon = issuedCouponJpaRepository.findByUserIdAndCouponId(input.userId, input.couponId)
                    .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "발급된 쿠폰"));
            issuedCoupon.validate(dateHolder);
            discountAmount = coupon.calculateDiscountAmount(totalAmount);
        }

        Long paidAmount = totalAmount - discountAmount;
        Wallet wallet = walletJpaRepository.findByUserId(input.userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "지갑"));
        wallet.pay(paidAmount, dateHolder);
        walletJpaRepository.save(wallet);

        Order order = Order.create(input.userId, totalAmount, discountAmount, paidAmount, dateHolder.now());
        Order savedOrder = orderJpaRepository.save(order);

        if (issuedCoupon != null) {
            issuedCoupon.use(savedOrder.getId());
            issuedCouponJpaRepository.save(issuedCoupon);
        }

        return Output.from(savedOrder);
    }


    private void validateProducts(List<Product> products, List<Input.OrderProduct> orderProducts) {
        if (products.size() != orderProducts.size()) {
            throw new CommonException(ErrorCode.INVALID_REQUEST, "주문한 상품 중 일부가 존재하지 않습니다.");
        }
    }

    private Long calculateTotalAmount(List<Product> products, List<Input.OrderProduct> orderProducts) {
        long total = 0L;
        for (Input.OrderProduct orderProduct : orderProducts) {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(orderProduct.productId()))
                    .findFirst()
                    .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "상품"));
            product.decreaseQuantity(orderProduct.quantity());
            productJpaRepository.save(product);
            total += product.getPrice() * orderProduct.quantity();
        }
        return total;
    }
}
