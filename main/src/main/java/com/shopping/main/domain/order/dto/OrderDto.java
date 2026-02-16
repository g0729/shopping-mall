package com.shopping.main.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "주문 요청")
@Getter
@Setter
public class OrderDto {
    @Schema(description = "상품 ID", example = "1")
    @NotNull(message = "상품 아이디는 필수 입력 값입니다.")
    private Long itemId;

    @Schema(description = "주문 수량", example = "2")
    @Min(value = 1, message = "최소 주문 수량은 1개 입니다.")
    @Max(value = 999, message = "최대 주문 수량은 999개 입니다.")
    private int count;
}
