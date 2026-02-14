package com.shopping.main.domain.payment.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("결제 대기"),
    PAID("결제 완료"),
    CANCEL("결제 취소"),
    FAILED("결제 실패");

    private final String description;

    public boolean canTransitTo(PaymentStatus next) {
        return switch (this) {
            case READY -> next == PAID || next == FAILED || next == CANCEL;
            case PAID -> next == CANCEL;
            case FAILED -> next == READY;
            case CANCEL -> false;
        };
    }
}