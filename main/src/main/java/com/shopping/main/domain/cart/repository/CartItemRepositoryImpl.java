package com.shopping.main.domain.cart.repository;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shopping.main.domain.cart.dto.CartDetailDto;
import com.shopping.main.domain.cart.entity.QCartItem;
import com.shopping.main.domain.product.entity.QProduct;
import com.shopping.main.domain.product.entity.QProductImage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CartItemRepositoryImpl implements CartItemRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CartDetailDto> findCartDetailDtoList(Long cartId) {
        QCartItem cartItem = QCartItem.cartItem;
        QProduct product = QProduct.product;
        QProductImage productImage = QProductImage.productImage;

        return jpaQueryFactory.select(Projections.constructor(CartDetailDto.class,
                cartItem.id,
                product.name,
                product.price,
                cartItem.count,
                productImage.imgUrl))
                .from(cartItem)
                .join(cartItem.product, product)
                .join(productImage).on(cartItem.product.eq(productImage.product))
                .where(cartItem.cart.id.eq(cartId)
                        .and(productImage.repImgYn.eq("Y")))
                .orderBy(cartItem.regTime.desc())
                .fetch();
    }
}
