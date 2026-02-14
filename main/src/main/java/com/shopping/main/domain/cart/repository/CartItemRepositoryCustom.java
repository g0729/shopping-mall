package com.shopping.main.domain.cart.repository;

import java.util.List;

import com.shopping.main.domain.cart.dto.CartDetailDto;

public interface CartItemRepositoryCustom {
    List<CartDetailDto> findCartDetailDtoList(Long cartId);
}
