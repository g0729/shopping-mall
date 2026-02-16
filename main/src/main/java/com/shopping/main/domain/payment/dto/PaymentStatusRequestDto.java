package com.shopping.main.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "결제 상태 변경 요청")
@Getter
@Setter
public class PaymentStatusRequestDto {
    @Schema(description = "주문 ID", example = "1")
    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;
}