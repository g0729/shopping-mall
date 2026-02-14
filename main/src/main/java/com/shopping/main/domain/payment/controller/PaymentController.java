package com.shopping.main.domain.payment.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopping.main.domain.order.service.OrderService;
import com.shopping.main.domain.payment.dto.PaymentConfirmRequestDto;
import com.shopping.main.domain.payment.dto.PaymentReadyRequestDto;
import com.shopping.main.domain.payment.dto.PaymentStatusRequestDto;
import com.shopping.main.domain.payment.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping("/ready")
    public ResponseEntity<?> ready(@RequestBody @Valid PaymentReadyRequestDto dto, Principal principal) {

        if (!orderService.validateOrder(dto.getOrderId(), principal.getName())) {
            return new ResponseEntity<>("결제 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(paymentService.ready(dto, principal.getName()));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody @Valid PaymentConfirmRequestDto dto, Principal principal) {

        if (!orderService.validateOrder(dto.getOrderId(), principal.getName())) {
            return new ResponseEntity<>("결제 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(paymentService.confirm(dto, principal.getName()));
    }

    @PostMapping("/fail")
    public ResponseEntity<?> fail(@RequestBody @Valid PaymentStatusRequestDto dto, Principal principal) {

        if (!orderService.validateOrder(dto.getOrderId(), principal.getName())) {
            return new ResponseEntity<>("권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(paymentService.fail(dto, principal.getName()));
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestBody @Valid PaymentStatusRequestDto dto, Principal principal) {

        if (!orderService.validateOrder(dto.getOrderId(), principal.getName())) {
            return new ResponseEntity<>("취소 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(paymentService.cancel(dto, principal.getName()));
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<?> getStatus(@PathVariable Long orderId, Principal principal) {

        if (!orderService.validateOrder(orderId, principal.getName())) {
            return new ResponseEntity<>("조회 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(paymentService.getStatus(orderId, principal.getName()));
    }
}
