package com.shopping.main.domain.product.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shopping.main.domain.product.constant.ProductSellStatus;
import com.shopping.main.domain.product.dto.ProductSummaryDto;
import com.shopping.main.domain.product.dto.ProductSearchDto;
import com.shopping.main.domain.product.dto.QProductSummaryDto;
import com.shopping.main.domain.product.entity.Product;
import com.shopping.main.domain.product.entity.QProduct;
import com.shopping.main.domain.product.entity.QProductImage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSummaryDto> findMainProductDto(ProductSearchDto dto, Pageable pageable) {
        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;

        // 1. 데이터 조회
        List<ProductSummaryDto> content = queryFactory.select(
                new QProductSummaryDto(
                        product.id,
                        product.name,
                        product.description,
                        productImage.imgUrl,
                        product.price))
                .from(productImage)
                .join(productImage.product, product)
                .where(productImage.repImgYn.eq("Y"),
                        itemNmLike(dto.getSearchQuery()))
                .orderBy(product.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 카운트 조회 (전체 개수 세기)
        Long total = queryFactory.select(Wildcard.count)
                .from(productImage)
                .join(productImage.product, product)
                .where(productImage.repImgYn.eq("Y"),
                        itemNmLike(dto.getSearchQuery()))
                .fetchOne();

        if (total == null)
            total = 0L;

        // 3. Page 객체로 변환해서 반환
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Product> findAdminItem(ProductSearchDto productSearchDto, Pageable pageable) {
        QProduct product = QProduct.product;

        // 1. 조건에 맞는 상품 조회
        List<Product> content = queryFactory
                .selectFrom(product) // 엔티티 전체를 가져옴 (수정/삭제를 위해)
                .where(regDtsAfter(productSearchDto.getSearchDateType()), // 날짜 조건
                        searchSellStatusEq(productSearchDto.getSearchSellStatus()), // 판매상태 조건
                        searchByLike(productSearchDto.getSearchBy(), productSearchDto.getSearchQuery())) // 검색어 조건
                .orderBy(product.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 개수 카운트
        Long total = queryFactory
                .select(Wildcard.count)
                .from(product)
                .where(regDtsAfter(productSearchDto.getSearchDateType()),
                        searchSellStatusEq(productSearchDto.getSearchSellStatus()),
                        searchByLike(productSearchDto.getSearchBy(), productSearchDto.getSearchQuery()))
                .fetchOne();

        if (total == null)
            total = 0L;

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression regDtsAfter(String searchDateType) {
        if (!StringUtils.hasText(searchDateType) || "all".equals(searchDateType)) {
            return null; // 조건 없음 (전체 조회)
        }

        LocalDateTime dateTime = LocalDateTime.now();

        if ("1d".equals(searchDateType))
            dateTime = dateTime.minusDays(1);
        else if ("1w".equals(searchDateType))
            dateTime = dateTime.minusWeeks(1);
        else if ("1m".equals(searchDateType))
            dateTime = dateTime.minusMonths(1);
        else if ("6m".equals(searchDateType))
            dateTime = dateTime.minusMonths(6);

        return QProduct.product.regTime.after(dateTime); // 해당 시간 이후에 등록된 것만
    }

    private BooleanExpression searchSellStatusEq(ProductSellStatus searchSellStatus) {
        // null이면 전체 조회, 아니면 상태가 같은지 비교
        return searchSellStatus == null ? null : QProduct.product.productSellStatus.eq(searchSellStatus);
    }

    // ③ 검색어 조건 (상품명 or 등록자ID)
    private BooleanExpression searchByLike(String searchBy, String searchQuery) {
        if (!StringUtils.hasText(searchQuery)) {
            return null; // 검색어가 없으면 무시
        }

        if ("itemNm".equals(searchBy)) {
            return QProduct.product.name.like("%" + searchQuery + "%");
        } else if ("createdBy".equals(searchBy)) {
            return QProduct.product.createdBy.like("%" + searchQuery + "%");
        }

        return null;
    }

    private BooleanExpression itemNmLike(String searchQuery) {
        if (!StringUtils.hasText(searchQuery)) {
            return null;
        }
        return QProduct.product.name.like("%" + searchQuery + "%");
    }

}
