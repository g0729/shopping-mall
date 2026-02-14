package com.shopping.main.domain.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.shopping.main.domain.product.constant.ProductSellStatus;
import com.shopping.main.domain.product.dto.ProductDtlDto;
import com.shopping.main.domain.product.dto.ProductFormDto;
import com.shopping.main.domain.product.dto.ProductSearchDto;
import com.shopping.main.domain.product.dto.ProductSummaryDto;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.repository.ProductRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
public class ProductServiceTest {

        @Autowired
        private ProductService productService;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private EntityManager em;

        @Test
        @DisplayName("상품 등록 성공")
        void saveProduct_success() throws Exception {
                ProductFormDto formDto = ProductFormDto.of(createProduct(
                                "테스트 상품",
                                10000,
                                100,
                                ProductSellStatus.SELL));

                MockMultipartFile file = new MockMultipartFile(
                                "file1", "test1.jpg", "image/jpeg", "test image".getBytes());

                List<MultipartFile> fileList = Arrays.asList(file);

                Long productId = productService.saveProduct(formDto, fileList);

                assertNotNull(productId);

                Product savedProduct = productRepository.findById(productId).orElseThrow();
                assertEquals(savedProduct.getName(), "테스트 상품");
                assertEquals(savedProduct.getPrice(), 10000);
                assertEquals(savedProduct.getStockQuantity(), 100);
                assertEquals(savedProduct.getProductSellStatus(), ProductSellStatus.SELL);

        }

        @Test
        @DisplayName("상품 상세 조회 성공")
        void getProductDtlView_success() throws Exception {
                ProductFormDto formDto = ProductFormDto.of(createProduct(
                                "테스트 상품",
                                10000,
                                100,
                                ProductSellStatus.SELL));

                MockMultipartFile file = new MockMultipartFile(
                                "file1", "test1.jpg", "image/jpeg", "test image".getBytes());

                List<MultipartFile> fileList = Arrays.asList(file);

                Long productId = productService.saveProduct(formDto, fileList);
                em.flush();
                em.clear();
                ProductDtlDto result = productService.getProductDtlView(productId);

                assertFalse(result.getProductImgDtoList().isEmpty());
        }

        @Test
        @DisplayName("존재하지 않는 상품 조회 실패")
        void getProductDtlView_fail_not_found() {
                Long nonExistedId = 99999L;

                assertThatThrownBy(() -> productService.getAdminProductDtl(nonExistedId))
                                .isInstanceOf(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("메인 상품 목록 조회 성공")
        void getMainProductList_success() throws Exception {
                for (int i = 1; i <= 3; i++) {
                        ProductFormDto formDto = ProductFormDto.of(createProduct(
                                        "테스트 상품" + i,
                                        10000 + i,
                                        100 + i,
                                        ProductSellStatus.SELL));

                        MockMultipartFile file = new MockMultipartFile(
                                        "file" + i, "test" + i + ".jpg", "image/jpeg", "test image".getBytes());

                        List<MultipartFile> fileList = Arrays.asList(file);

                        productService.saveProduct(formDto, fileList);
                }

                em.flush();
                em.clear();
                ProductSearchDto searchDto = new ProductSearchDto();
                Pageable pageable = PageRequest.of(0, 10);
                Page<ProductSummaryDto> result = productService.getMainProductList(searchDto, pageable);

                assertNotNull(result);
                assertEquals(result.getContent().size(), 3);

        }

        @Test
        @DisplayName("상품 수정 성공")
        void updateProduct_success() throws Exception {
                ProductFormDto formDto = ProductFormDto.of(createProduct(
                                "테스트 상품",
                                10000,
                                100,
                                ProductSellStatus.SELL));

                MockMultipartFile file = new MockMultipartFile(
                                "file1", "test1.jpg", "image/jpeg", "test image".getBytes());

                List<MultipartFile> fileList = Arrays.asList(file);

                Long productId = productService.saveProduct(formDto, fileList);

                ProductFormDto updateDto = productService.getAdminProductDtl(productId);
                updateDto.setName("수정 후 상품");
                updateDto.setPrice(15000);
                updateDto.setStockQuantity(50);
                updateDto.setDescription("수정 후 설명");

                Long updatedId = productService.updateProduct(updateDto, fileList);

                Product updatedProduct = productRepository.findById(updatedId).orElseThrow();

                assertEquals(updatedProduct.getName(), "수정 후 상품");
                assertEquals(updatedProduct.getPrice(), 15000);
                assertEquals(updatedProduct.getStockQuantity(), 50);
                assertEquals(updatedProduct.getDescription(), "수정 후 설명");

        }

        @Test
        @DisplayName("관리자 상품 페이지 조회 성공")
        void getAdminProductPage_success() throws Exception {
                ProductFormDto formDto = ProductFormDto.of(createProduct(
                                "테스트 상품",
                                10000,
                                100,
                                ProductSellStatus.SELL));

                MockMultipartFile file = new MockMultipartFile(
                                "file1", "test1.jpg", "image/jpeg", "test image".getBytes());

                List<MultipartFile> fileList = Arrays.asList(file);

                productService.saveProduct(formDto, fileList);

                ProductSearchDto productSearchDto = new ProductSearchDto();
                Pageable pageable = PageRequest.of(0, 10);
                Page<Product> result = productService.getAdminProductPage(productSearchDto, pageable);

                assertNotNull(result);
                assertThat(result.getContent()).isNotEmpty();
        }

        private Product createProduct(String name, int price, int stockQuantity, ProductSellStatus sellStatus) {
                Product product = new Product();
                product.setName(name);
                product.setPrice(price);
                product.setDescription(name + " 의 설명입니다");
                product.setStockQuantity(stockQuantity);
                product.setProductSellStatus(sellStatus);
                return product;
        }
}
