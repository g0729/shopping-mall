package com.shopping.main.domain.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.shopping.main.domain.product.dto.ProductSummaryDto;
import com.shopping.main.domain.product.dto.ProductSearchDto;
import com.shopping.main.domain.product.entity.Product;

public interface ProductRepositoryCustom {
    Page<ProductSummaryDto> findMainProductDto(ProductSearchDto productSearchDto, Pageable pageable);

    Page<Product> findAdminItem(ProductSearchDto productSearchDto, Pageable pageable);
}
