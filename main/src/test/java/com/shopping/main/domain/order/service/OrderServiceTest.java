package com.shopping.main.domain.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.main.domain.order.constant.OrderStatus;
import com.shopping.main.domain.order.dto.OrderDto;
import com.shopping.main.domain.order.dto.OrderHistDto;
import com.shopping.main.domain.order.entity.Order;
import com.shopping.main.domain.order.repository.OrderRepository;
import com.shopping.main.domain.product.constant.ProductSellStatus;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.exception.OutOfStockException;
import com.shopping.main.domain.product.repository.ProductRepository;
import com.shopping.main.domain.user.constant.UserRole;
import com.shopping.main.domain.user.entity.SiteUser;
import com.shopping.main.domain.user.repository.UserRepository;

@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    SiteUser user;
    Product product;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                SiteUser.builder()
                        .email("order@test.com")
                        .nickname("tester")
                        .password("test-password")
                        .role(UserRole.USER)
                        .provider("local")
                        .providerId(null)
                        .build());

        product = new Product();
        product.setName("상품A");
        product.setPrice(10000);
        product.setStockQuantity(10);
        product.setDescription("테스트 상품 설명");
        product.setProductSellStatus(ProductSellStatus.SELL);
        product = productRepository.save(product);
    }

    @Test
    @DisplayName("주문 성공 시 재고 차감")
    void orderSuccess_decreaseStock() {

        OrderDto orderDto = createOrderDto(product.getId(), 3);

        Long orderId = orderService.order(orderDto, user.getEmail());

        Product updated = productRepository.findById(product.getId()).orElseThrow();
        Order savedOrder = orderRepository.findById(orderId).orElseThrow();

        assertEquals(7, updated.getStockQuantity());
        assertEquals(OrderStatus.ORDER, savedOrder.getStatus());

    }

    @Test
    @DisplayName("재고 부족 주문 실패")
    public void orderFail_outOfStock() {
        OrderDto orderDto = createOrderDto(product.getId(), 11);

        assertThrows(OutOfStockException.class, () -> orderService.order(orderDto, user.getEmail()));
    }

    @Test
    @DisplayName("주문 취소 시 재고 복구")
    public void cancelOrder_restoreStock() {

        OrderDto orderDto = createOrderDto(product.getId(), 4);

        Long orderId = orderService.order(orderDto, user.getEmail());
        orderService.cancelOrder(orderId);

        Product restored = productRepository.findById(product.getId()).orElseThrow();
        Order cancledOrder = orderRepository.findById(orderId).orElseThrow();

        assertEquals(10, restored.getStockQuantity());
        assertEquals(OrderStatus.CANCEL, cancledOrder.getStatus());
    }

    @Test
    @DisplayName("주문 이력 조회는 user_id 기준으로 본인 주문만 조회한다")
    void getOrderList_returnsOnlyOwnersOrders() {
        SiteUser otherUser = userRepository.save(
                SiteUser.builder()
                        .email("order-other@test.com")
                        .nickname("other")
                        .password("test-password")
                        .role(UserRole.USER)
                        .provider("local")
                        .providerId(null)
                        .build());

        Long userOrderId1 = orderService.order(createOrderDto(product.getId(), 1), user.getEmail());
        Long userOrderId2 = orderService.order(createOrderDto(product.getId(), 1), user.getEmail());
        Long otherOrderId = orderService.order(createOrderDto(product.getId(), 1), otherUser.getEmail());

        Page<OrderHistDto> result = orderService.getOrderList(user.getEmail(), PageRequest.of(0, 10));
        Set<Long> orderIds = result.getContent().stream().map(OrderHistDto::getOrderId).collect(Collectors.toSet());

        assertEquals(2L, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(Set.of(userOrderId1, userOrderId2), orderIds);
        assertFalse(orderIds.contains(otherOrderId));
    }

    private OrderDto createOrderDto(Long productId, int count) {
        OrderDto dto = new OrderDto();
        dto.setItemId(productId);
        dto.setCount(count);
        return dto;
    }
}
