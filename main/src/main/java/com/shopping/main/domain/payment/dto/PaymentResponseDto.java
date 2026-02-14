package com.shopping.main.domain.payment.dto;

import com.shopping.main.domain.payment.entity.Payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponseDto {
    private Long paymentId;
    private Long orderId;
    private String status;
    private String impUid;
    private String merchantUid;
    private int amount;

    public static PaymentResponseDto of(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .status(payment.getStatus().name())
                .impUid(payment.getImpUid())
                .merchantUid(payment.getMerchantUid())
                .amount(payment.getAmount())
                .build();
    }
}
