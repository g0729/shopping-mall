package com.shopping.main.domain.product.dto;

import com.shopping.main.domain.product.entity.ProductImage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImgDto {

    private Long id;

    private String imgName;

    private String oriImgName;

    private String imgUrl;

    private String repImgYn; // 대표 이미지 여부

    private static final long serialVersionUID = 1L;

    public static ProductImgDto of(ProductImage productImage) {
        ProductImgDto dto = new ProductImgDto();
        dto.setId(productImage.getId());
        dto.setImgName(productImage.getImgName());
        dto.setOriImgName(productImage.getOriImgName());
        dto.setImgUrl(productImage.getImgUrl());
        dto.setRepImgYn(productImage.getRepImgYn());

        return dto;
    }
}
