package com.shopping.main.domain.product.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import com.shopping.main.domain.product.constant.ProductSellStatus;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.entity.ProductImage;
import com.shopping.main.domain.product.repository.ProductImageRepository;
import com.shopping.main.domain.product.repository.ProductRepository;

@Tag("performance")
@SpringBootTest
@ActiveProfiles("test")
public class CachePerformanceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private CacheManager cacheManager;

    private Long productId;

    @BeforeEach
    void setUp() {
        cacheManager.getCache("productDetail").clear();

        Product product = new Product();
        product.setName("캐시 테스트 상품");
        product.setPrice(10000);
        product.setStockQuantity(100);
        product.setDescription("캐시 성능 테스트용 상품입니다.");
        product.setProductSellStatus(ProductSellStatus.SELL);
        product = productRepository.save(product);
        productId = product.getId();

        ProductImage img = new ProductImage();
        img.setProduct(product);
        img.setRepImgYn("Y");
        img.setImgName("test.jpg");
        img.setOriImgName("test_ori.jpg");
        img.setImgUrl("/images/test.jpg");
        productImageRepository.save(img);
    }

    @Test
    @DisplayName("Redis 캐싱 성능 비교 - Cache Miss vs Cache Hit")
    void cachePerfomranceComparison() {
        int iterations = 10000;

        // 1 . Cache Miss (DB 조회)
        cacheManager.getCache("productDetail").clear();
        long start1 = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            cacheManager.getCache("productDetail").clear();
            productService.getProductDtlView(productId);
        }
        long missTotal = (System.nanoTime() - start1) / 1_000_000;

        // 2. Cache Hit(Redis 조회)
        productService.getProductDtlView(productId); // 워밍업

        long start2 = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            productService.getProductDtlView(productId);
        }

        long hitTotal = (System.nanoTime() - start2) / 1_000_000;

        System.out.println("===== Redis 캐싱 성능 테스트 결과 =====");
        System.out.println("반복 횟수: " + iterations);
        System.out.println("Cache Miss (DB): 총 " + missTotal + "ms, 평균 " + (missTotal / iterations) + "ms");
        System.out.println("Cache Hit (Redis): 총 " + hitTotal + "ms, 평균 " + (hitTotal / iterations) + "ms");
        System.out.println("성능 개선: " + String.format("%.1f", (double) missTotal / hitTotal) + "배");

    }
}
