package com.shopping.main.domain.payment.gateway;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.shopping.main.domain.payment.gateway.dto.PaymentVerificationResult;

import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnProperty(name = "payment.gateway", havingValue = "toss")
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {
    private final RestClient tossRestClient;

    @Override
    public PaymentVerificationResult verify(String paymentKey, String orderId, int amount) {
        Map<String, Object> request = new HashMap<>();
        request.put("paymentKey", paymentKey);
        request.put("orderId", orderId);
        request.put("amount", amount);

        JsonNode body = post("/v1/payments/confirm", request, "결제 승인 실패");

        String status = body.path("status").asText();
        if (!"DONE".equals(status)) {
            throw new IllegalArgumentException("토스 결제 상태가 DONE이 아닙니다: " + status);

        }

        return new PaymentVerificationResult(
                body.path("paymentKey").asText(),
                body.path("orderId").asText(),
                body.path("totalAmount").asInt());
    }

    @Override
    public void cancel(String paymentKey, int amount, String reason) {
        Map<String, Object> request = new HashMap<>();
        request.put("cancelReason", (reason == null) || reason.isBlank() ? "고객 요청" : reason);
        request.put("cancelAmount", amount);

        post("/v1/payments/" + paymentKey + "/cancel", request, "결제 취소 실패");
    }

    private JsonNode post(String uri, Object requestBody, String errorMessage) {

        try {
            JsonNode body = tossRestClient.post()
                    .uri(uri)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null) {
                throw new IllegalArgumentException(errorMessage + " 응답 없음");

            }
            return body;
        } catch (RestClientResponseException exception) {
            throw new IllegalArgumentException(errorMessage + " - " + exception.getResponseBodyAsString(), exception);
        }
    }
}
