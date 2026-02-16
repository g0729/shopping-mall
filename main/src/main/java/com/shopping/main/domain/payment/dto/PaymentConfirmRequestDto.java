package com.shopping.main.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "결제 승인 요청")
@Getter
@Setter
public class PaymentConfirmRequestDto {
    @Schema(description = "주문 ID", example = "1")
    @NotNull(message = "주문 ID 는 필수입니다.")
    private Long orderId;

    @Schema(description = "PG사 결제 고유 ID", example = "imp_1234567890")
    @NotBlank(message = "impUid는 필수입니다.")
    private String impUid;

    @Schema(description = "가맹점 주문번호", example = "order_1_1234567890")
    @NotBlank(message = "merchantUid는 필수입니다.")
    private String merchantUid;

    @Schema(description = "결제 금액", example = "50000")
    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
    private Integer amount;
}
