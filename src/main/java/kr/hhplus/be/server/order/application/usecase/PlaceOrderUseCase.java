package kr.hhplus.be.server.order.application.usecase;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.common.event.EventPublisher;
import kr.hhplus.be.server.common.exception.CommonException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.lock.DistributedLock;
import kr.hhplus.be.server.common.lock.resolver.ProductAndWalletLockKeyResolver;
import kr.hhplus.be.server.common.time.DateHolder;
import kr.hhplus.be.server.coupon.domain.entity.OrderProduct;
import kr.hhplus.be.server.order.application.service.CouponPricingService;
import kr.hhplus.be.server.order.domain.entity.DiscountInfo;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderItem;
import kr.hhplus.be.server.order.domain.event.PlacedOrderEvent;
import kr.hhplus.be.server.order.domain.repository.OrderJpaRepository;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductJpaRepository;
import kr.hhplus.be.server.wallet.application.service.WalletCommandService;
import lombok.RequiredArgsConstructor;
import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceOrderUseCase {

    public record Input(
            Long userId,
            Long couponId,
            List<OrderProduct> orderProduct
    ) {
        public List<Long> getOrderProductIds() {
            return orderProduct.stream().map(OrderProduct::productId).toList();
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
    private final ProductJpaRepository productJpaRepository;
    private final CouponPricingService couponPricingService;
    private final WalletCommandService walletCommandService;
    private final DateHolder dateHolder;
    private final EventPublisher eventPublisher;

    @Transactional
    @DistributedLock(resolver = ProductAndWalletLockKeyResolver.class, waitTime = 1L, leaseTime = 10L)
    public Output execute(Input input)
            throws OptimisticEntityLockException
    {
        Map<Long, Product> products = productJpaRepository.findAllById(input.getOrderProductIds()).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderProduct orderProduct : input.orderProduct) {
            Product product = Optional.ofNullable(products.get(orderProduct.productId()))
                    .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_RESOURCE, "상품"));
            boolean isSuccess = productJpaRepository.decreaseProductQuantity(product.getId(), orderProduct.quantity()) == 1;
            if (!isSuccess) {
                throw new CommonException(ErrorCode.NOT_ENOUGH_PRODUCT_QUANTITY, product.getName());
            }
            orderItems.add(OrderItem.of(product.getId(), orderProduct.quantity(), product.getPrice(), dateHolder));
        }

        DiscountInfo discountInfo = couponPricingService.applyCouponPricing(input.couponId, input.userId, orderItems);

        Order order = Order.create(input.userId, orderItems, discountInfo.discountAmount(), discountInfo.issuedCouponId());

        walletCommandService.use(input.userId, order.getPaidAmount());

        Order savedOrder = orderJpaRepository.save(order);

        eventPublisher.publish("order.created", new PlacedOrderEvent(input.orderProduct));

        return Output.from(savedOrder);
    }

}
