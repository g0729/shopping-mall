// package com.shopping.main.domain.payment.gateway;

// import java.util.HashMap;
// import java.util.Map;

// import
// org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.stereotype.Component;
// import org.springframework.web.client.RestClient;
// import org.springframework.web.client.RestClientResponseException;

// import com.fasterxml.jackson.databind.JsonNode;
// import
// com.shopping.main.domain.payment.gateway.dto.PaymentVerificationResult;

// import lombok.RequiredArgsConstructor;

// @Component
// @ConditionalOnProperty(name = "payment.gateway", havingValue = "toss")
// @RequiredArgsConstructor
// public class TossPaymentGateway implements PaymentGateway {
// private final RestClient tossRestClient;

// @Override
// public PaymentVerificationResult verify(String impUid, String merchanUid, int
// amount) {
// Map<String, Object> request = new HashMap<>();
// request.put("paymentKey", impUid);
// request.put("orderId", merchanUid);
// request.put("amount", amount);

// JsonNode body = post("/v1/payments/confirm", request, "결제 승인 실패");

// String status = body.path("status").asText();
// if (!"DONE".equals(status)) {
// throw new IllegalArgumentException("토스 결제 상태가 DONE이 아닙니다. status = " +
// status);
// }

// String paymentKey = body.path("paymentKey").asText();
// String orderId = body.path("orderId").asText();
// int totalAmount = body.path("totalAmount").asInt(-1);

// if (paymentKey.isBlank() || orderId.isBlank() || totalAmount < 0) {
// throw new IllegalArgumentException("토스 승인 응답 값이 올바르지 않습니다.");
// }

// return new PaymentVerificationResult(paymentKey, orderId, totalAmount);
// }

// @Override
// public void cancel(String impUid, int amount, String reason) {
// Map<String, Object> request = new HashMap<>();
// request.put("cancelRason", (reason == null || reason.isBlank()) ?
// "USER_REQUEST" : reason);
// request.put("cancelAmount", amount);

// JsonNode body = post("/v1/payments/{")
// }

// private JsonNode post(String uri, Object requestBody, String errorMessage,
// Object... uriVars) {
// try {
// JsonNode body = tossRestClient.post()
// .uri(uri, uriVars)
// .body(requestBody)
// .retrieve()
// .body(JsonNode.class);

// if (body == null) {
// throw new IllegalArgumentException(errorMessage + "(응답 없음)");
// }
// return body;
// } catch (RestClientResponseException e) {
// throw new IllegalArgumentException(errorMessage + " - " +
// e.getResponseBodyAsString(), e);
// }
// }
// }
