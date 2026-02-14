package com.shopping.main.domain.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopping.main.domain.product.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderByIdAsc(Long productId);

    ProductImage findByProductIdAndRepImgYn(Long productId, String repImgYn);

    // 상품 ID 목록으로 대표 이미지 일괄 조회
    @Query("select pi from ProductImage pi " +
            "where pi.product.id in :productIds and pi.repImgYn = 'Y'")
    List<ProductImage> findRepImagesByProductIds(@Param("productIds") List<Long> productIds);

}
