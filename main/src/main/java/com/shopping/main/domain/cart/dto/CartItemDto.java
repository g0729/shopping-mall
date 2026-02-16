package com.shopping.main.domain.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "장바구니 상품 추가 요청")
@Getter
@Setter
public class CartItemDto {
    @Schema(description = "상품 ID", example = "1")
    @NotNull(message = "상품 아이디는 필수 입력 값 입니다.")
    private Long itemId;

    @Schema(description = "수량", example = "1")
    @Min(value = 1, message = "최소 1개 이상 담아주세요.")
    private int count;
}
