package com.shopping.main.domain.payment.dto;

import com.shopping.main.domain.payment.entity.Payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "결제 응답")
@Getter
@Builder
public class PaymentResponseDto {
    @Schema(description = "결제 ID", example = "1")
    private Long paymentId;

    @Schema(description = "주문 ID", example = "1")
    private Long orderId;

    @Schema(description = "결제 상태", example = "PAID")
    private String status;

    @Schema(description = "PG사 결제 고유 ID", example = "imp_1234567890")
    private String impUid;

    @Schema(description = "가맹점 주문번호", example = "order_1_1234567890")
    private String merchantUid;

    @Schema(description = "결제 금액", example = "50000")
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
