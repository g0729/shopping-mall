package com.shopping.main.domain.cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopping.main.domain.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long>, CartItemRepositoryCustom {

    CartItem findByCartIdAndProductId(Long cartId, Long productId);

    // 장바구니 아이템 + 상품 한번에 조회
    @Query("select ci from CartItem ci " +
            "join fetch ci.product " +
            "where ci.id in :ids")
    List<CartItem> findAllWithProdcutByIds(@Param("ids") List<Long> ids);
}
