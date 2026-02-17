package com.shopping.main.domain.payment.gateway.mock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.shopping.main.domain.payment.gateway.PaymentGateway;
import com.shopping.main.domain.payment.gateway.dto.PaymentVerificationResult;

@Component
@ConditionalOnProperty(name = "payment.gateway", havingValue = "mock", matchIfMissing = true)
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentVerificationResult verify(String impUid, String merchantUid, int amount) {
        return new PaymentVerificationResult(impUid, merchantUid, 0);
    }

    @Override
    public void cancel(String impUid, int amount, String reason) {
        // mock 단계에서는 외부 호출 없음
    }
}
