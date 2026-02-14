package com.shopping.main.domain.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.shopping.main.domain.order.dto.OrderDto;
import com.shopping.main.domain.order.repository.OrderRepository;
import com.shopping.main.domain.product.constant.ProductSellStatus;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.repository.ProductRepository;
import com.shopping.main.domain.user.constant.UserRole;
import com.shopping.main.domain.user.entity.SiteUser;
import com.shopping.main.domain.user.repository.UserRepository;

@Tag("performance")
@SpringBootTest
public class OrderConcurrencyTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Long productId;
    private String email;

    @BeforeEach
    void setUp() {
        SiteUser user = userRepository.save(
                SiteUser.builder()
                        .email("concurrency@test.com")
                        .nickname("currencyTester")
                        .password("test-password")
                        .role(UserRole.USER)
                        .provider("local")
                        .providerId(null)
                        .build());

        email = user.getEmail();

        Product product = new Product();
        product.setName("동시성 테스트 상품");
        product.setPrice(100000);
        product.setStockQuantity(100);
        product.setDescription("동시성 테스트용 상품ㅁ");
        product.setProductSellStatus(ProductSellStatus.SELL);
        product = productRepository.save(product);
        productId = product.getId();
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("비관적 락 - 100명 동시 주문 시 재고 정합성 보장")
    void pessimisticLock_100ConcurrentOrders() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderDto orderDto = new OrderDto();
                    orderDto.setItemId(productId);
                    orderDto.setCount(1);
                    orderService.order(orderDto, email);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });

        }

        latch.await();
        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();
        System.out.println("===== 동시성 테스트 결과 =====");
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
        System.out.println("남은 재고: " + product.getStockQuantity());

        assertEquals(100, successCount.get());
        assertEquals(0, failCount.get());
        assertEquals(0, product.getStockQuantity());

    }

    @Test
    @DisplayName("비관적 락 - 재고 초과 주문 시 정확히 재고만큼만 성공")
    void pessimisticLock_overStockOrders() throws InterruptedException {
        int threadCount = 150;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderDto orderDto = new OrderDto();
                    orderDto.setItemId(productId);
                    orderDto.setCount(1);
                    orderService.order(orderDto, email);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();

        System.out.println("===== 재고 초과 동시성 테스트 결과 =====");
        System.out.println("성공: " + successCount.get());
        System.out.println("실패(재고부족): " + failCount.get());
        System.out.println("남은 재고: " + product.getStockQuantity());

        assertEquals(100, successCount.get());
        assertEquals(50, failCount.get());
        assertEquals(0, product.getStockQuantity());
    }
}
