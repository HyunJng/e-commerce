package kr.hhplus.be.server.order.application.usecase;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.order.application.service.CouponPricingService;
import kr.hhplus.be.server.order.domain.entity.DiscountInfo;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderItem;
import kr.hhplus.be.server.order.domain.repository.OrderJpaRepository;
import kr.hhplus.be.server.product.application.service.ProductLockingQueryService;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.wallet.application.service.WalletCommandService;
import lombok.RequiredArgsConstructor;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaceOrderUseCase {

    public record Input(
            Long userId,
            Long couponId,
            List<OrderProduct> orderProduct
    ) {
        public record OrderProduct(
                Long productId,
                Integer quantity
        ) {
        }

        public List<Long> getOrderProductIds() {
            return orderProduct.stream().map(Input.OrderProduct::productId).toList();
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

    private final OrderJpaRepository orderJpaRepository;
    private final ProductLockingQueryService productLockingQueryService;
    private final CouponPricingService couponPricingService;
    private final WalletCommandService walletCommandService;
    private final DateHolder dateHolder;

    @Transactional
    @DistributedLock(resolver = OrderLockResolver.class, waitTime = 10L, leaseTime = 5L)
    public Output execute(Input input)
            throws OptimisticEntityLockException
    {
        // 상품 정보 조회
        Map<Long, Product> products = productLockingQueryService.findProducts(input.getOrderProductIds());

        List<OrderItem> orderItems = new ArrayList<>();
        for (Input.OrderProduct orderProduct : input.orderProduct) {
            Product product = Optional.ofNullable(products.get(orderProduct.productId))
                    .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "상품"));
            // 상품 수량 감소
            product.decreaseQuantity(orderProduct.quantity());
            orderItems.add(OrderItem.of(product.getId(), orderProduct.quantity, product.getPrice(), dateHolder));
        }

        // 할인 정보 조회
        DiscountInfo discountInfo = couponPricingService.applyCouponPricing(input.couponId, input.userId, orderItems);

        // 주문 생성
        Order order = Order.create(input.userId, orderItems, discountInfo.discountAmount(), discountInfo.issuedCouponId());

        // 지갑에서 결제 금액 차감
        walletCommandService.use(input.userId, order.getPaidAmount());

        // 주문 저장
        Order savedOrder = orderJpaRepository.save(order);

        return Output.from(savedOrder);
    }

}
