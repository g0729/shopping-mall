package com.shopping.main.domain.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.shopping.main.domain.order.constant.OrderStatus;
import com.shopping.main.domain.order.entity.Order;
import com.shopping.main.domain.order.entity.OrderItem;
import com.shopping.main.domain.order.repository.OrderRepository;
import com.shopping.main.domain.payment.constant.PaymentStatus;
import com.shopping.main.domain.payment.dto.PaymentConfirmRequestDto;
import com.shopping.main.domain.payment.dto.PaymentReadyRequestDto;
import com.shopping.main.domain.payment.dto.PaymentResponseDto;
import com.shopping.main.domain.payment.dto.PaymentStatusRequestDto;
import com.shopping.main.domain.payment.entity.Payment;
import com.shopping.main.domain.payment.repository.PaymentRepository;
import com.shopping.main.domain.product.constant.ProductSellStatus;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.repository.ProductRepository;
import com.shopping.main.domain.user.constant.UserRole;
import com.shopping.main.domain.user.entity.SiteUser;
import com.shopping.main.domain.user.repository.UserRepository;

@SpringBootTest
@Transactional
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("결제 준비 성공: 주문자 본인만 READY 처리 가능")
    void readySuccess_ownerOnly() {
        SiteUser owner = createUser("pay-owner@test.com");
        Order order = createOrder(owner, 12000, 2);

        PaymentResponseDto response = paymentService.ready(createReadyDto(order.getId()), owner.getEmail());

        assertEquals(order.getId(), response.getOrderId());
        assertEquals(PaymentStatus.READY.name(), response.getStatus());
        assertEquals(order.getTotalPrice(), response.getAmount());
    }

    @Test
    @DisplayName("결제 준비 실패: 타인 주문 접근 차단")
    void readyFail_notOwner() {
        SiteUser owner = createUser("pay-owner2@test.com");
        SiteUser other = createUser("pay-other2@test.com");
        Order order = createOrder(owner, 10000, 1);

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.ready(createReadyDto(order.getId()), other.getEmail()));
    }

    @Test
    @DisplayName("결제 승인 성공: READY -> PAID")
    void confirmSuccess() {
        SiteUser owner = createUser("pay-owner3@test.com");
        Order order = createOrder(owner, 15000, 2);
        paymentService.ready(createReadyDto(order.getId()), owner.getEmail());

        PaymentResponseDto response = paymentService.confirm(
                createConfirmDto(order.getId(), "imp-001", "merchant-001", order.getTotalPrice()),
                owner.getEmail());

        assertEquals(PaymentStatus.PAID.name(), response.getStatus());
        assertEquals("imp-001", response.getImpUid());
        assertEquals("merchant-001", response.getMerchantUid());
        assertEquals(order.getTotalPrice(), response.getAmount());
    }

    @Test
    @DisplayName("결제 승인 실패: 다른 주문에서 이미 사용된 impUid")
    void confirmFail_duplicateImpUid() {
        SiteUser owner = createUser("pay-owner4@test.com");
        Order firstOrder = createOrder(owner, 8000, 1);
        Order secondOrder = createOrder(owner, 9000, 1);

        paymentService.ready(createReadyDto(firstOrder.getId()), owner.getEmail());
        paymentService.confirm(
                createConfirmDto(firstOrder.getId(), "dup-imp-001", "merchant-a", firstOrder.getTotalPrice()),
                owner.getEmail());

        paymentService.ready(createReadyDto(secondOrder.getId()), owner.getEmail());

        assertThrows(IllegalArgumentException.class, () -> paymentService.confirm(
                createConfirmDto(secondOrder.getId(), "dup-imp-001", "merchant-b", secondOrder.getTotalPrice()),
                owner.getEmail()));
    }

    @Test
    @DisplayName("결제 승인 멱등 처리: 동일 impUid/금액 재요청 시 기존 결제 반환")
    void confirmIdempotent_sameImpUidAndAmount() {
        SiteUser owner = createUser("pay-owner5@test.com");
        Order order = createOrder(owner, 11000, 1);

        paymentService.ready(createReadyDto(order.getId()), owner.getEmail());
        PaymentResponseDto first = paymentService.confirm(
                createConfirmDto(order.getId(), "idem-imp-001", "merchant-idem", order.getTotalPrice()),
                owner.getEmail());

        PaymentResponseDto second = paymentService.confirm(
                createConfirmDto(order.getId(), "idem-imp-001", "merchant-idem-2", order.getTotalPrice()),
                owner.getEmail());

        assertEquals(first.getPaymentId(), second.getPaymentId());
        assertEquals(PaymentStatus.PAID.name(), second.getStatus());
        assertEquals("idem-imp-001", second.getImpUid());
        assertEquals(order.getTotalPrice(), second.getAmount());
    }

    @Test
    @DisplayName("결제 취소 실패: FAILED 상태는 취소 불가")
    void cancelFail_failedPayment() {
        SiteUser owner = createUser("pay-owner6@test.com");
        Order order = createOrder(owner, 7000, 1);
        Payment payment = createPayment(order, PaymentStatus.FAILED);

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.cancel(createStatusDto(order.getId()), owner.getEmail()));
        assertEquals(PaymentStatus.FAILED, paymentRepository.findById(payment.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("결제 실패 처리 제한: PAID 상태는 FAILED로 변경 불가")
    void failFail_paidPayment() {
        SiteUser owner = createUser("pay-owner7@test.com");
        Order order = createOrder(owner, 13000, 1);
        createPayment(order, PaymentStatus.PAID);

        assertThrows(IllegalArgumentException.class,
                () -> paymentService.fail(createStatusDto(order.getId()), owner.getEmail()));
    }

    private SiteUser createUser(String email) {
        return userRepository.save(SiteUser.builder()
                .email(email)
                .nickname(email.split("@")[0])
                .password("test-password")
                .role(UserRole.USER)
                .provider("local")
                .providerId(null)
                .build());
    }

    private Order createOrder(SiteUser user, int price, int count) {
        Product product = new Product();
        product.setName("결제테스트상품-" + user.getEmail());
        product.setPrice(price);
        product.setStockQuantity(100);
        product.setDescription("결제 테스트용 상품");
        product.setProductSellStatus(ProductSellStatus.SELL);
        productRepository.save(product);

        OrderItem orderItem = OrderItem.createOrderItem(product, count);
        Order order = Order.createOrder(user, java.util.List.of(orderItem));
        order.setStatus(OrderStatus.ORDER);
        return orderRepository.save(order);
    }

    private Payment createPayment(Order order, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalPrice());
        payment.setStatus(status);
        payment.setImpUid("seed-imp-" + order.getId());
        payment.setMerchantUid("seed-merchant-" + order.getId());
        return paymentRepository.save(payment);
    }

    private PaymentReadyRequestDto createReadyDto(Long orderId) {
        PaymentReadyRequestDto dto = new PaymentReadyRequestDto();
        dto.setOrderId(orderId);
        return dto;
    }

    private PaymentConfirmRequestDto createConfirmDto(Long orderId, String impUid, String merchantUid, int amount) {
        PaymentConfirmRequestDto dto = new PaymentConfirmRequestDto();
        dto.setOrderId(orderId);
        dto.setImpUid(impUid);
        dto.setMerchantUid(merchantUid);
        dto.setAmount(amount);
        return dto;
    }

    private PaymentStatusRequestDto createStatusDto(Long orderId) {
        PaymentStatusRequestDto dto = new PaymentStatusRequestDto();
        dto.setOrderId(orderId);
        return dto;
    }
}
