package com.shopping.main.domain.cart.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "장바구니 주문 요청")
@Getter
@Setter
public class CartOrderDto {
    @Schema(description = "장바구니 아이템 ID", example = "1")
    private Long cartItemId;

    @Schema(description = "주문할 장바구니 아이템 목록")
    private List<CartOrderDto> cartOrderDtoList;
}
