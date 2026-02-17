package com.shopping.main.domain.payment.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.shopping.main.domain.order.dto.OrderHistDto;
import com.shopping.main.domain.order.service.OrderService;
import com.shopping.main.domain.payment.dto.PaymentConfirmRequestDto;
import com.shopping.main.domain.payment.dto.PaymentResponseDto;
import com.shopping.main.domain.payment.dto.PaymentStatusRequestDto;
import com.shopping.main.domain.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentViewController {
    private final OrderService orderService;
    private final PaymentService paymentService;

    @Value("${toss.client-key:}")
    private String tossClientKey;

    @GetMapping("/{orderId}")
    public String checkout(@PathVariable("orderId") Long orderId,
            Principal principal,
            Model model) {
        if (!orderService.validateOrder(orderId, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "주문에 대한 권한이 없습니다");
        }

        OrderHistDto orderHistDto = orderService.getOrderDetail(orderId, principal.getName());
        model.addAttribute("order", orderHistDto);
        model.addAttribute("tossClientKey", tossClientKey);
        return "payment/checkout";
    }

    // 토스 결제 성공 콜백
    @GetMapping("/toss/success")
    public String tossSuccess(@RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam int amount,
            @RequestParam Long shopOrderId,
            Principal principal) {

        PaymentConfirmRequestDto dto = new PaymentConfirmRequestDto();
        dto.setOrderId(shopOrderId);
        dto.setImpUid(paymentKey);
        dto.setMerchantUid(orderId);
        dto.setAmount(amount);

        paymentService.confirm(dto, principal.getName());
        return "redirect:/payments/success?orderId=" + shopOrderId;
    }

    // 토스 결제 실패 콜백
    @GetMapping("/toss/fail")
    public String tossFail(@RequestParam String code,
            @RequestParam String message,
            @RequestParam Long shopOrderId,
            Principal principal) {

        PaymentStatusRequestDto dto = new PaymentStatusRequestDto();
        dto.setOrderId(shopOrderId);
        paymentService.fail(dto, principal.getName());

        return "redirect:/payments/fail?orderId=" + shopOrderId
                + "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }

    // 결제 성공 페이지
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam("orderId") Long orderId,
            Principal principal,
            Model model) {
        if (!orderService.validateOrder(orderId, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "주문에 대한 권한이 없습니다");
        }

        PaymentResponseDto paymentDto = paymentService.getStatus(orderId, principal.getName());
        model.addAttribute("payment", paymentDto);
        model.addAttribute("orderId", orderId);
        return "payment/success";
    }

    // 결제 실패 페이지
    @GetMapping("/fail")
    public String paymentFail(@RequestParam("orderId") Long orderId,
            @RequestParam(value = "message", required = false) String message,
            Principal principal,
            Model model) {
        if (!orderService.validateOrder(orderId, principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "주문에 대한 권한이 없습니다");
        }

        model.addAttribute("orderId", orderId);
        model.addAttribute("message", message != null ? message : "결제에 실패했습니다.");
        return "payment/fail";
    }
}
