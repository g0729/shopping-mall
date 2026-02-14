package com.shopping.main.domain.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.shopping.main.domain.cart.dto.CartOrderDto;
import com.shopping.main.domain.cart.entity.CartItem;
import com.shopping.main.domain.cart.repository.CartItemRepository;
import com.shopping.main.domain.order.dto.OrderDto;
import com.shopping.main.domain.order.dto.OrderHistDto;
import com.shopping.main.domain.order.dto.OrderItemDto;
import com.shopping.main.domain.order.entity.Order;
import com.shopping.main.domain.order.entity.OrderItem;
import com.shopping.main.domain.order.repository.OrderRepository;
import com.shopping.main.domain.payment.constant.PaymentStatus;
import com.shopping.main.domain.payment.entity.Payment;
import com.shopping.main.domain.payment.repository.PaymentRepository;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.entity.ProductImage;
import com.shopping.main.domain.product.repository.ProductImageRepository;
import com.shopping.main.domain.product.repository.ProductRepository;
import com.shopping.main.domain.user.entity.SiteUser;
import com.shopping.main.domain.user.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductImageRepository productImageRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentRepository paymentRepository;

    public Long order(OrderDto orderDto, String email) {

        // 1. 주문할 상품 조회
        Product product = productRepository.findByIdForUpdate(orderDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        // 2. 현재 로그인 한 회원 조회
        SiteUser siteUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다"));

        // 3. 주문 엔티티 생성
        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem = OrderItem.createOrderItem(product, orderDto.getCount());
        orderItemList.add(orderItem);

        Order order = Order.createOrder(siteUser, orderItemList);

        orderRepository.save(order);

        return order.getId();
    }

    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {

        // 1 . 주문 목록 조회
        List<Order> orders = orderRepository.findOrders(email, pageable);

        // 2. 전체 주문 개수 조회
        Long totalCount = orderRepository.countOrder(email);

        if (orders.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, totalCount);
        }

        // Fetch Join 으로 OrderItems + Product 일괄 로드
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        List<Order> ordersWithItems = orderRepository.findOrdersWithItems(orderIds);

        List<Long> productIds = ordersWithItems.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .map(oi -> oi.getProduct().getId())
                .distinct().toList();

        // 대표 이미지 일괄 조회
        Map<Long, String> repImgMap = productImageRepository.findRepImagesByProductIds(productIds).stream()
                .collect(Collectors.toMap(pi -> pi.getProduct().getId(), ProductImage::getImgUrl));

        // 결제 정보 일괄 조회
        Map<Long, PaymentStatus> paymentMap = paymentRepository.findByOrdersIds(orderIds).stream()
                .collect(Collectors.toMap(p -> p.getOrder().getId(), Payment::getStatus));

        // DTO 변환

        List<OrderHistDto> orderHistDtoList = new ArrayList<>();
        for (Order order : ordersWithItems) {
            OrderHistDto orderHistDto = OrderHistDto.of(order);
            List<OrderItem> orderItems = order.getOrderItems();

            for (OrderItem orderItem : orderItems) {
                String imgUrl = repImgMap.getOrDefault(orderItem.getProduct().getId(), "");
                orderHistDto.addOrderItemDto(OrderItemDto.of(orderItem, imgUrl));
            }

            // 결제 정보 조회 및 설정
            PaymentStatus status = paymentMap.get(order.getId());

            if (status != null) {
                orderHistDto.setPaymentStatus(status);
            }
            orderHistDtoList.add(orderHistDto);
        }

        return new PageImpl<>(orderHistDtoList, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String email) {
        SiteUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다"));

        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        SiteUser savedUser = order.getUser();

        if (!StringUtils.equals(user.getEmail(), savedUser.getEmail())) {
            return false;
        }

        return true;
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);

        order.cancelOrder();
    }

    public Long orders(List<CartOrderDto> cartItemDtoList, String email) {
        SiteUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        // 장바구니 아이템 + 상품 일괄 fetch
        List<Long> cartItemIds = cartItemDtoList.stream().map(CartOrderDto::getCartItemId).toList();
        List<CartItem> cartItems = cartItemRepository.findAllWithProdcutByIds(cartItemIds);

        // 상품 비관적 락 일괄 조회
        List<Long> productIds = cartItems.stream().map(ci -> ci.getProduct().getId()).distinct().toList();
        Map<Long, Product> lockedProducts = productRepository.findAllByIdForUpdate(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 주문할 아이템 리스트 생성
        List<OrderItem> orderItemList = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = lockedProducts.get(cartItem.getProduct().getId());
            OrderItem orderItem = OrderItem.createOrderItem(product, cartItem.getCount());
            orderItemList.add(orderItem);
        }

        Order order = Order.createOrder(user, orderItemList);
        orderRepository.save(order);

        cartItemRepository.deleteAll(cartItems);
        return order.getId();
    }

    @Transactional(readOnly = true)
    public OrderHistDto getOrderDetail(Long orderId, String email) {

        // Fetch Join 으로 한번에 조회
        Order order = orderRepository.findOrderWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다"));
        OrderHistDto orderHistDto = OrderHistDto.of(order);

        // 대표 이미지 일괄 조회
        List<Long> productIds = order.getOrderItems().stream()
                .map(oi -> oi.getProduct().getId()).toList();

        Map<Long, String> repImgMap = productImageRepository.findRepImagesByProductIds(productIds).stream()
                .collect(Collectors.toMap(pi -> pi.getProduct().getId(), ProductImage::getImgUrl));

        for (OrderItem orderItem : order.getOrderItems()) {
            String imgUrl = repImgMap.getOrDefault(orderItem.getProduct().getId(), "");
            OrderItemDto orderItemDto = OrderItemDto.of(orderItem, imgUrl);
            orderHistDto.addOrderItemDto(orderItemDto);
        }

        // 결제 정보 조회 및 설정
        paymentRepository.findByOrderId(orderId)
                .ifPresent(payment -> orderHistDto.setPaymentStatus(payment.getStatus()));

        return orderHistDto;
    }
}
