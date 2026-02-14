package com.shopping.main.domain.product.dto;

import java.util.ArrayList;
import java.util.List;

import com.shopping.main.domain.product.constant.ProductSellStatus;
import com.shopping.main.domain.product.entity.Product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductFormDto {

    private Long id;

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String name;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    @NotBlank(message = "상품 상세 설명은 필수 입력 값입니다.")
    private String description;

    @NotNull(message = "재고는 필수 입력 값입니다.")
    private Integer stockQuantity;

    private ProductSellStatus productSellStatus;

    private List<ProductImgDto> productImgDtoList = new ArrayList<>();

    private List<Long> productImgIds = new ArrayList<>();

    public static ProductFormDto of(Product product) {
        ProductFormDto dto = new ProductFormDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setDescription(product.getDescription());
        dto.setProductSellStatus(product.getProductSellStatus());
        return dto;
    }

    public Product toEntity() {
        Product product = new Product();
        product.setName(this.name);
        product.setPrice(this.price);
        product.setStockQuantity(this.stockQuantity);
        product.setDescription(this.description);
        product.setProductSellStatus(this.productSellStatus);
        return product;
    }

}