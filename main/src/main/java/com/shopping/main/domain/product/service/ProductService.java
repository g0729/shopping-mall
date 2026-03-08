package com.shopping.main.domain.product.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.main.domain.product.dto.ProductDtlDto;
import com.shopping.main.domain.product.dto.ProductFormDto;
import com.shopping.main.domain.product.dto.ProductImgDto;
import com.shopping.main.domain.product.dto.ProductSearchDto;
import com.shopping.main.domain.product.dto.ProductSummaryDto;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.entity.ProductImage;
import com.shopping.main.domain.product.repository.ProductRepository;
import com.shopping.main.global.common.RestPage;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImgService productImgService;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PRODUCT_LIST_CACHE = "productList::";
    private static final String PRODUCT_DETAIL_CACHE = "productDetail::";
    private static final String LOCK_PREFIX = "lock:";
    private static final int BASE_TTL = 1800;
    private static final int JITTER_RANGE = 300;

    @CacheEvict(value = "productList", allEntries = true)
    public Long saveProduct(ProductFormDto productFormDto, List<MultipartFile> productImgFileList) throws Exception {

        Product product = productFormDto.toEntity();
        productRepository.save(product);

        for (int i = 0; i < productImgFileList.size(); i++) {
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setRepImgYn(i == 0 ? "Y" : "N");
            productImgService.saveProductImg(productImage, productImgFileList.get(i));
        }
        return product.getId();
    }

    @Transactional(readOnly = true)
    public Page<ProductSummaryDto> getMainProductList(ProductSearchDto productSearchDto, Pageable pageable) {
        String cacheKey = PRODUCT_LIST_CACHE + productSearchDto.toString() + "_" + pageable.getPageNumber();
        String lockKey = LOCK_PREFIX + cacheKey;

        // 1. 캐시 조회
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<RestPage<ProductSummaryDto>>() {});
            } catch (Exception e) {
                log.warn("productList 캐시 역직렬화 실패, DB 조회로 폴백: {}", e.getMessage());
            }
        }

        // 2. 분산 락 시도
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (acquired) {
                try {
                    // Double-checked locking: 락 획득 사이에 다른 스레드가 이미 캐시를 채웠을 수 있음
                    cached = stringRedisTemplate.opsForValue().get(cacheKey);
                    if (cached != null) {
                        return objectMapper.readValue(cached, new TypeReference<RestPage<ProductSummaryDto>>() {});
                    }

                    Page<ProductSummaryDto> result = productRepository.findMainProductDto(productSearchDto, pageable);
                    int ttl = BASE_TTL + ThreadLocalRandom.current().nextInt(0, JITTER_RANGE);
                    stringRedisTemplate.opsForValue().set(
                            cacheKey, objectMapper.writeValueAsString(result), ttl, TimeUnit.SECONDS);
                    return result;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("productList 캐시 처리 중 오류, DB 조회로 폴백: {}", e.getMessage());
        }

        // Fallback: 락 획득 실패 시 DB 직접 조회
        return productRepository.findMainProductDto(productSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    public ProductDtlDto getProductDtlView(Long productId) {
        String cacheKey = PRODUCT_DETAIL_CACHE + productId;
        String lockKey = LOCK_PREFIX + cacheKey;

        // 1. 캐시 조회
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, ProductDtlDto.class);
            } catch (Exception e) {
                log.warn("productDetail 캐시 역직렬화 실패, DB 조회로 폴백: {}", e.getMessage());
            }
        }

        // 2. 분산 락 시도
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (acquired) {
                try {
                    // Double-checked locking
                    cached = stringRedisTemplate.opsForValue().get(cacheKey);
                    if (cached != null) {
                        return objectMapper.readValue(cached, ProductDtlDto.class);
                    }

                    ProductDtlDto productDtlDto = buildProductDtlDto(productId);
                    int ttl = BASE_TTL + ThreadLocalRandom.current().nextInt(0, JITTER_RANGE);
                    stringRedisTemplate.opsForValue().set(
                            cacheKey, objectMapper.writeValueAsString(productDtlDto), ttl, TimeUnit.SECONDS);
                    return productDtlDto;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("productDetail 캐시 처리 중 오류, DB 조회로 폴백: {}", e.getMessage());
        }

        // Fallback
        return buildProductDtlDto(productId);
    }

    @Transactional(readOnly = true)
    public Page<Product> getAdminProductPage(ProductSearchDto productSearchDto, Pageable pageable) {
        return productRepository.findAdminItem(productSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    public ProductFormDto getAdminProductDtl(Long productId) {

        Product product = productRepository.findWithImagesById(productId).orElseThrow(EntityNotFoundException::new);

        List<ProductImgDto> productImgDtoList = product.getProductImages()
                .stream()
                .map(pi -> ProductImgDto.of(pi))
                .toList();

        ProductFormDto productFormDto = ProductFormDto.of(product);
        productFormDto.setProductImgDtoList(productImgDtoList);
        for (ProductImage productImage : product.getProductImages()) {
            productFormDto.getProductImgIds().add(productImage.getId());
        }

        return productFormDto;
    }

    @CacheEvict(value = { "productDetail", "productList" }, allEntries = true)
    public Long updateProduct(ProductFormDto productFormDto, List<MultipartFile> productImgFileList) throws Exception {

        Product product = productRepository.findById(productFormDto.getId()).orElseThrow(EntityNotFoundException::new);

        product.updateProduct(productFormDto);

        List<Long> productImgIds = productFormDto.getProductImgIds();

        if (productImgIds != null && !productImgIds.isEmpty()) {
            int loopCount = Math.min(productImgFileList.size(), productImgIds.size());
            for (int i = 0; i < loopCount; i++) {
                productImgService.updateProductImg(productImgIds.get(i), productImgFileList.get(i));
            }
        }
        return product.getId();
    }

    private ProductDtlDto buildProductDtlDto(Long productId) {
        Product product = productRepository.findWithImagesById(productId).orElseThrow(EntityNotFoundException::new);
        List<ProductImgDto> productImages = product.getProductImages()
                .stream()
                .map(ProductImgDto::of)
                .toList();
        ProductDtlDto dto = ProductDtlDto.of(product);
        dto.setProductImgDtoList(productImages);
        return dto;
    }
}
