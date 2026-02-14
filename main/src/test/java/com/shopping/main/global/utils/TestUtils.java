package com.shopping.main.global.utils;

import com.shopping.main.domain.product.constant.ProductSellStatus;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.entity.ProductImage;
import com.shopping.main.domain.product.repository.ProductImageRepository;
import com.shopping.main.domain.product.repository.ProductRepository;

public class TestUtils {

    /**
     * 더미 상품과 이미지를 생성하여 DB에 저장하는 정적 메서드
     * 
     * @param productRepository      상품 저장소
     * @param productImageRepository 이미지 저장소
     * @param startNum               상품 번호 시작값 (예: 1)
     * @param endNum                 상품 번호 끝값 (예: 30)
     */
    public static void createDummyProducts(ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            int startNum, int endNum) {

        for (int i = startNum; i <= endNum; i++) {

            // 1. 상품(Product) 생성
            Product product = new Product();
            product.setName("테스트 상품 " + i);
            product.setPrice(10000 + (i * 1000)); // 가격을 다양하게
            product.setDescription("이것은 테스트 상품 " + i + " 입니다. 아주 훌륭한 품질을 자랑합니다.");
            product.setStockQuantity(100);
            product.setProductSellStatus(ProductSellStatus.SELL);

            productRepository.save(product); // 상품 먼저 저장 (ID 생성)

            // 2. 이미지(ProductImage) 생성
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product); // 연관관계 설정
            productImage.setRepImgYn("Y"); // 대표 이미지 여부

            productImage.setImgUrl("/images/dummy.jpg");
            productImage.setImgName("dummy.jpg");
            productImage.setOriImgName("dummy.jpg");

            productImageRepository.save(productImage); // 이미지 저장
        }
    }
}