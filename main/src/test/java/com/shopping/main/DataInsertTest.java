package com.shopping.main;

import com.shopping.main.domain.product.repository.ProductImageRepository;
import com.shopping.main.domain.product.repository.ProductRepository;
import com.shopping.main.global.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@Tag("performance")
@SpringBootTest
@Transactional
class DataInsertTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductImageRepository productImageRepository;

    @Test
    @DisplayName("더미 데이터 30개 넣기")
    @Rollback(false)
    void insertDummies() {
        // Utils를 호출해서 1번부터 30번까지 상품을 만듭니다.
        TestUtils.createDummyProducts(productRepository, productImageRepository, 1, 30);

        System.out.println("=================================");
        System.out.println("더미 데이터 30개 생성 완료!");
        System.out.println("=================================");
    }
}