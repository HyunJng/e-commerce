package kr.hhplus.be.server.medium.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.coupon.domain.IssuedCoupon;
import kr.hhplus.be.server.coupon.domain.IssuedCouponJpaRepository;
import kr.hhplus.be.server.medium.AbstractIntegrationTest;
import kr.hhplus.be.server.order.controller.dto.OrderApi;
import kr.hhplus.be.server.order.domain.Order;
import kr.hhplus.be.server.order.domain.OrderItems;
import kr.hhplus.be.server.order.domain.OrderItemsJpaRepository;
import kr.hhplus.be.server.order.domain.OrderJpaRepository;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductJpaRepository;
import kr.hhplus.be.server.wallet.domain.Wallet;
import kr.hhplus.be.server.wallet.domain.WalletJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SqlGroup(value = {
        @Sql(value = "/sql/delete-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/order-integration-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
})
public class OrderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderJpaRepository orderJpaRepository;
    @Autowired
    private OrderItemsJpaRepository orderItemsJpaRepository;
    @Autowired
    private ProductJpaRepository productJpaRepository;
    @Autowired
    private IssuedCouponJpaRepository issuedCouponJpaRepository;
    @Autowired
    private WalletJpaRepository walletJpaRepository;

    @Test
    void 주문에_성공하면_주문정보가_저장된다() throws Exception {
        // given
        Long userId = 1L;
        OrderApi.Request request = new OrderApi.Request(
                userId,
                null,
                List.of(
                        new OrderApi.Request.OrderProduct(1L, 10),
                        new OrderApi.Request.OrderProduct(2L, 5)
                )
        );

        String content = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        // then
        Order order = orderJpaRepository.findByUserId(userId).get(0);
        List<OrderItems> orderItems = orderItemsJpaRepository.findByOrderId(order.getId());
        Map<Long, Product> products = productJpaRepository.findAllById(List.of(1L, 2L)).stream().collect(
                Collectors.toMap(
                        product -> product.getId(),
                        product -> product
                )
        );

        assertThat(order).isNotNull();
        assertThat(orderItems).isNotEmpty();

        assertThat(order.getUserId()).isEqualTo(userId);
        assertThat(order.getIssuedCouponId()).isNull();
        assertThat(orderItems).hasSize(2);
        assertThat(orderItems.get(0).getProductId()).isEqualTo(1L);
        assertThat(orderItems.get(0).getQuantity()).isEqualTo(10);
        assertThat(orderItems.get(0).getUnitPrice()).isEqualTo(products.get(1L).getPrice());
        assertThat(orderItems.get(0).getTotalPrice()).isEqualTo(products.get(1L).getPrice() * 10);
        assertThat(orderItems.get(1).getProductId()).isEqualTo(2L);
        assertThat(orderItems.get(1).getQuantity()).isEqualTo(5);
        assertThat(orderItems.get(1).getUnitPrice()).isEqualTo(products.get(2L).getPrice());
        assertThat(orderItems.get(1).getTotalPrice()).isEqualTo(products.get(2L).getPrice() * 5);
        assertThat(order.getTotalAmount()).isEqualTo(
                orderItems.get(0).getTotalPrice() + orderItems.get(1).getTotalPrice()
        );
    }

    @Test
    void 주문에_성공하면_잔액이_사용금액만큼_줄어든다() throws Exception {
        // given
        Long userId = 1L;
        OrderApi.Request request = new OrderApi.Request(
                userId,
                null,
                List.of(
                        new OrderApi.Request.OrderProduct(1L, 10),
                        new OrderApi.Request.OrderProduct(2L, 5)
                )
        );

        String content = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        // then
        long expectedPaidAmount = 10 * 1000L + 5 * 2000L; // 10개 * 1000원 + 5개 * 2000원
        Wallet wallet = walletJpaRepository.findByUserId(userId).orElse(null);

        assertThat(wallet).isNotNull();
        assertThat(wallet.getBalance()).isEqualTo(100000L - expectedPaidAmount); // 초기 잔액 100000원
    }

    @Test
    void 주문에_성공하면_상품정보가_요청수량만큼_줄어든다() throws Exception {
        // given
        Long userId = 1L;
        OrderApi.Request request = new OrderApi.Request(
                userId,
                null,
                List.of(
                        new OrderApi.Request.OrderProduct(1L, 10),
                        new OrderApi.Request.OrderProduct(2L, 5)
                )
        );

        String content = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        // then
        Map<Long, Product> products = productJpaRepository.findAllById(List.of(1L, 2L)).stream().collect(
                Collectors.toMap(
                        product -> product.getId(),
                        product -> product
                )
        );

        assertThat(products.get(1L).getQuantity()).isEqualTo(0); // 10 - 10
        assertThat(products.get(2L).getQuantity()).isEqualTo(15); // 20 - 5
    }

    @Test
    void 주문할_때_쿠폰을_사용하면_쿠폰_상태가_사용으로_변경되고_주문정보에_쿠폰정보도_저장된다() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = 1L;
        OrderApi.Request request = new OrderApi.Request(
                userId,
                couponId,
                List.of(new OrderApi.Request.OrderProduct(1L, 10))
        );

        String content = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        // then
        IssuedCoupon issuedCoupon = issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId).orElse(null);
        Order order = orderJpaRepository.findByUserId(userId).get(0);

        assertThat(issuedCoupon).isNotNull();
        assertThat(order).isNotNull();

        assertThat(issuedCoupon.getStatus()).isEqualTo(IssuedCoupon.Status.USED);
        assertThat(order.getIssuedCouponId()).isEqualTo(couponId);
    }

    @Test
    void 주문_중_오류가_발생되면_주문정보가_저장되지_않는다() throws Exception {
        // given
        Long userId = 1L;

        OrderApi.Request request = new OrderApi.Request(
                userId,
                null,
                List.of(
                        // 잔액 부족
                        new OrderApi.Request.OrderProduct(1L, 10),
                        new OrderApi.Request.OrderProduct(2L, 20),
                        new OrderApi.Request.OrderProduct(3L, 30)
                )
        );

        String content = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultMsg").value(ErrorCode.INVALID_REQUEST.getMessage("잔액 부족")));

        // then
        List<Order> orders = orderJpaRepository.findByUserId(userId);

        assertThat(orders).isEmpty();
    }

    @Test
    void 쿠폰을_사용한_주문_중_오류가_발생되면_쿠폰정보가_사용상태로_변경되지_않는다() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = 1L;

        OrderApi.Request request = new OrderApi.Request(
                userId,
                couponId,
                List.of(
                        // 잔액 부족
                        new OrderApi.Request.OrderProduct(1L, 10),
                        new OrderApi.Request.OrderProduct(2L, 20),
                        new OrderApi.Request.OrderProduct(3L, 30)
                )
        );

        String content = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultMsg").value(ErrorCode.INVALID_REQUEST.getMessage("잔액 부족")));

        // then
        IssuedCoupon issuedCoupon = issuedCouponJpaRepository.findByUserIdAndCouponId(userId, couponId).orElse(null);

        assertThat(issuedCoupon).isNotNull();
        assertThat(issuedCoupon.getStatus()).isEqualTo(IssuedCoupon.Status.ACTIVE);
    }

    @Test
    void 주문_중_오류가_발생되면_잔액정보가_변경되지_않는다() throws Exception {
        // given
        Long userId = 1L;

        OrderApi.Request request = new OrderApi.Request(
                userId,
                null,
                List.of(
                        // 잔액 부족
                        new OrderApi.Request.OrderProduct(1L, 10),
                        new OrderApi.Request.OrderProduct(2L, 20),
                        new OrderApi.Request.OrderProduct(3L, 30)
                )
        );

        String content = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultMsg").value(ErrorCode.INVALID_REQUEST.getMessage("잔액 부족")));

        // then
        Wallet wallet = walletJpaRepository.findByUserId(userId).orElse(null);

        assertThat(wallet).isNotNull();
        assertThat(wallet.getBalance()).isEqualTo(100000L); // 초기 잔액 100000원
    }

    @Test
    void 주문_중_오류가_발생되면_상품_수량이_변경되지_않는다() throws Exception {
        // given
        Long userId = 1L;
        Long couponId = 1L;

        OrderApi.Request request = new OrderApi.Request(
                userId,
                couponId,
                List.of(
                        // 잔액 부족
                        new OrderApi.Request.OrderProduct(1L, 10),
                        new OrderApi.Request.OrderProduct(2L, 20),
                        new OrderApi.Request.OrderProduct(3L, 30)
                )
        );

        String content = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultMsg").value(ErrorCode.INVALID_REQUEST.getMessage("잔액 부족")));

        // then
        Map<Long, Product> products = productJpaRepository.findAllById(List.of(1L, 2L, 3L)).stream().collect(
                Collectors.toMap(
                        product -> product.getId(),
                        product -> product
                )
        );

        assertThat(products.get(1L).getQuantity()).isEqualTo(10); // 초기 수량 10
        assertThat(products.get(2L).getQuantity()).isEqualTo(20); // 초기 수량 20
        assertThat(products.get(3L).getQuantity()).isEqualTo(30); // 초기 수량 30
    }

}
