package com.shopping.main.domain.payment.gateway.dto;

public record PaymentVerificationResult(
        String impUid,
        String merchantUid,
        int amount) {
}