package com.shopping.main.domain.product.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.shopping.main.domain.product.dto.ProductDtlDto;
import com.shopping.main.domain.product.dto.ProductFormDto;
import com.shopping.main.domain.product.dto.ProductImgDto;
import com.shopping.main.domain.product.dto.ProductSearchDto;
import com.shopping.main.domain.product.dto.ProductSummaryDto;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.entity.ProductImage;
import com.shopping.main.domain.product.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImgService productImgService;

    /**
     * 상품 등록 처리
     * 
     * @param productFormDto     : 화면에서 넘어온 상품 정보
     * @param productImgFileList : 화면에서 넘어온 이미지 파일 리스트 (여러 장)
     */

    @CacheEvict(value = "productList", allEntries = true)
    public Long saveProduct(ProductFormDto productFormDto, List<MultipartFile> productImgFileList) throws Exception {

        // 1. 상품 등록
        Product product = productFormDto.toEntity();
        productRepository.save(product);

        // 2. 이미지 등록

        for (int i = 0; i < productImgFileList.size(); i++) {
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);

            // 첫번째는 대표 이미지
            if (i == 0)
                productImage.setRepImgYn("Y");
            else
                productImage.setRepImgYn("N");

            productImgService.saveProductImg(productImage, productImgFileList.get(i));
        }
        return product.getId();
    }

    @Cacheable(value = "productList", key = "#productSearchDto.toString() + '_' + #pageable.pageNumber")
    @Transactional(readOnly = true)
    public Page<ProductSummaryDto> getMainProductList(ProductSearchDto productSearchDto, Pageable pageable) {
        return productRepository.findMainProductDto(productSearchDto, pageable);
    }

    @Cacheable(value = "productDetail", key = "#productId")
    @Transactional(readOnly = true)
    public ProductDtlDto getProductDtlView(Long productId) {
        // 1. 해당 상품의 이미지 리스트 조회
        Product product = productRepository.findWithImagesById(productId).orElseThrow(EntityNotFoundException::new);
        List<ProductImgDto> productImages = product.getProductImages()
                .stream()
                .map(pi -> ProductImgDto.of(pi))
                .toList();

        ProductDtlDto productDtlDto = ProductDtlDto.of(product);
        productDtlDto.setProductImgDtoList(productImages);
        return productDtlDto;
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
}
