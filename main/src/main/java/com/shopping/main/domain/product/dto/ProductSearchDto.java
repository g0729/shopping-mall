package com.shopping.main.domain.product.dto;

import java.io.Serializable;

import com.shopping.main.domain.product.constant.ProductSellStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchDto implements Serializable {

    // 현재 시간과 상품 등록일을 비교해서 데이터를 조회 (all, 1d, 1w, 1m, 6m)
    private String searchDateType = "all";

    // 상품의 판매상태를 기준으로 조회 (null, SELL, SOLD_OUT)
    private ProductSellStatus searchSellStatus;

    // 상품을 조회할 때 어떤 유형으로 조회할지 선택 (itemNm, createdBy)
    private String searchBy = "itemNm";

    // 조회할 검색어 저장할 변수
    private String searchQuery = "";

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return searchDateType + "_" + searchSellStatus + "_" + searchBy + "_" + searchQuery;
    }

}