package com.shopping.main.domain.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentReadyRequestDto {
    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;
}
