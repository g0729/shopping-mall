package com.shopping.main.domain.payment.gateway;

import com.shopping.main.domain.payment.gateway.dto.PaymentVerificationResult;

public interface PaymentGateway {
    PaymentVerificationResult verify(String impUid, String merchantUid);

    void cancel(String impUid, int amount, String reason);
}
