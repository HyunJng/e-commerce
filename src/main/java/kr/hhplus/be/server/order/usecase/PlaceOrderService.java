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
import kr.hhplus.be.server.order.domain.OrderItems;
import kr.hhplus.be.server.order.domain.OrderItemsJpaRepository;
import kr.hhplus.be.server.order.domain.OrderJpaRepository;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductJpaRepository;
import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.WalletJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
                    order.getCreateAt()
            );
        }
    }

    private record DiscountInfo(Long discountAmount, IssuedCoupon issuedCoupon) {

        public Long findIssuedCouponId() {
            return this.issuedCoupon != null ? issuedCoupon.getId() : null;
        }
    }


    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemsJpaRepository orderItemsJpaRepository;
    private final ProductJpaRepository productJpaRepository;
    private final CouponJpaRepository couponJpaRepository;
    private final IssuedCouponJpaRepository issuedCouponJpaRepository;
    private final WalletJpaRepository walletJpaRepository;
    private final DateHolder dateHolder;

    @Transactional
    public Output execute(Input input) {
        // 상품 정보 조회
        Map<Long, Product> orderProducts = productJpaRepository.findAllById(
                input.orderProduct.stream().map(Input.OrderProduct::productId).toList()
        ).stream().collect(Collectors.toMap(
                Product::getId,
                product -> product
        ));

        // 상품 존재 여부 검증
        validateProducts(orderProducts, input.orderProduct);

        // 상품 수량 감소 및 총 금액 계산
        Long totalAmount = calculateTotalAmount(orderProducts, input.orderProduct);

        // 쿠폰 정보 조회 및 할인 금액 계산
        DiscountInfo discountInfo = (input.couponId != null) ? getDiscountInfo(input, totalAmount) : new DiscountInfo(0L, null);
        Long discountAmount = discountInfo.discountAmount();
        Long issuedCouponId = discountInfo.findIssuedCouponId();

        // 지갑에서 결제 금액 차감
        Long paidAmount = totalAmount - discountAmount;

        Wallet wallet = walletJpaRepository.findByUserId(input.userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "지갑"));
        wallet.pay(paidAmount, dateHolder);
        walletJpaRepository.save(wallet);

        // 주문 생성 및 저장
        Order order = Order.create(input.userId, totalAmount, discountAmount, paidAmount, issuedCouponId);
        Order savedOrder = orderJpaRepository.save(order);

        for(Input.OrderProduct orderProduct : input.orderProduct) {
            OrderItems orderItems = OrderItems.create(
                    order,
                    orderProduct.productId,
                    orderProduct.quantity,
                    orderProducts.get(orderProduct.productId).getPrice()
            );
            orderItemsJpaRepository.save(orderItems);
        }

        return Output.from(savedOrder);
    }

    private void validateProducts(Map<Long, Product> products, List<Input.OrderProduct> orderProducts) {
        if (products.size() != orderProducts.size()) {
            throw new CommonException(ErrorCode.INVALID_REQUEST, "주문한 상품 중 일부가 존재하지 않습니다.");
        }
    }

    private DiscountInfo getDiscountInfo(Input input, Long totalAmount) {
        Coupon coupon = couponJpaRepository.findById(input.couponId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "쿠폰"));
        IssuedCoupon issuedCoupon = issuedCouponJpaRepository.findByUserIdAndCouponId(input.userId, input.couponId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "발급된 쿠폰"));
        issuedCoupon.validate(dateHolder);
        Long discountAmount = coupon.calculateDiscountAmount(totalAmount);
        issuedCoupon.use();
        issuedCouponJpaRepository.save(issuedCoupon);
        return new DiscountInfo(discountAmount, issuedCoupon);
    }

    private Long calculateTotalAmount(Map<Long, Product> products, List<Input.OrderProduct> orderProducts) {
        long total = 0L;
        for (Input.OrderProduct orderProduct : orderProducts) {
            Product product = Optional.ofNullable(products.get(orderProduct.productId()))
                    .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "상품"));
            product.decreaseQuantity(orderProduct.quantity());
            productJpaRepository.save(product);
            total += product.getPrice() * orderProduct.quantity();
        }
        return total;
    }
}
