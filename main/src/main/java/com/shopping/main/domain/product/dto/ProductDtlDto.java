package com.shopping.main.domain.product.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.shopping.main.domain.product.constant.ProductSellStatus;
import com.shopping.main.domain.product.entity.Product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductDtlDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private Integer price;
    private String description;
    private Integer stockQuantity;
    private ProductSellStatus productSellStatus;
    private List<ProductImgDto> productImgDtoList = new ArrayList<>();

    public static ProductDtlDto of(Product product) {
        ProductDtlDto productDtlDto = new ProductDtlDto();
        productDtlDto.setId(product.getId());
        productDtlDto.setName(product.getName());
        productDtlDto.setPrice(product.getPrice());
        productDtlDto.setDescription(product.getDescription());
        productDtlDto.setStockQuantity(product.getStockQuantity());
        productDtlDto.setProductSellStatus(product.getProductSellStatus());
        return productDtlDto;
    }
}
