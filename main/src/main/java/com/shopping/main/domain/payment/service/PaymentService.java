package com.shopping.main.domain.payment.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.shopping.main.domain.order.constant.OrderStatus;
import com.shopping.main.domain.order.entity.Order;
import com.shopping.main.domain.order.repository.OrderRepository;
import com.shopping.main.domain.payment.constant.PaymentStatus;
import com.shopping.main.domain.payment.dto.PaymentConfirmRequestDto;
import com.shopping.main.domain.payment.dto.PaymentReadyRequestDto;
import com.shopping.main.domain.payment.dto.PaymentResponseDto;
import com.shopping.main.domain.payment.dto.PaymentStatusRequestDto;
import com.shopping.main.domain.payment.entity.Payment;
import com.shopping.main.domain.payment.gateway.PaymentGateway;
import com.shopping.main.domain.payment.gateway.dto.PaymentVerificationResult;
import com.shopping.main.domain.payment.repository.PaymentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public PaymentResponseDto ready(PaymentReadyRequestDto dto, String email) {
        Order order = getOwnedOrder(dto.getOrderId(), email);
        validateOrderPayable(order);

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElseGet(() -> createPayment(order));
        if (payment.getStatus() == PaymentStatus.PAID || payment.getStatus() == PaymentStatus.CANCEL) {
            throw new IllegalArgumentException("이미 결제 완료되었거나 취소된 주문입니다");
        }

        payment.setAmount(order.getTotalPrice());
        payment.setMerchantUid("SHOP_" + order.getId() + "_" + System.currentTimeMillis());
        transitionTo(payment, PaymentStatus.READY);
        paymentRepository.save(payment);

        return PaymentResponseDto.of(payment);
    }

    public PaymentResponseDto confirm(PaymentConfirmRequestDto dto, String email) {
        Order order = getOwnedOrder(dto.getOrderId(), email);
        validateOrderPayable(order);

        if (order.getTotalPrice() != dto.getAmount()) {
            throw new IllegalArgumentException("결제 금액과 주문 금액이 일치하지 않습니다");
        }

        PaymentVerificationResult verified = paymentGateway.verify(dto.getImpUid(), dto.getMerchantUid(),
                order.getTotalPrice());
        if (verified.amount() > 0 && verified.amount() != order.getTotalPrice()) {
            throw new IllegalArgumentException("PG 검증 금액과 주문 금액이 일치하지 않습니다");
        }

        Optional<Payment> duplicated = paymentRepository.findByImpUid(verified.impUid());
        if (duplicated.isPresent() && !duplicated.get().getOrder().getId().equals(order.getId())) {
            throw new IllegalArgumentException("이미 사용된 impUid 입니다");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElseGet(() -> createPayment(order));

        if (payment.getStatus() == PaymentStatus.PAID) {
            if (verified.impUid().equals(payment.getImpUid()) && payment.getAmount() == order.getTotalPrice()) {
                return PaymentResponseDto.of(payment);
            }
            throw new IllegalArgumentException("이미 결제 완료된 주문입니다");
        }

        if (payment.getStatus() == PaymentStatus.CANCEL) {
            throw new IllegalArgumentException("취소된 결제는 승인할 수 없습니다");
        }

        payment.setImpUid(verified.impUid());
        payment.setMerchantUid(verified.merchantUid());
        payment.setAmount(order.getTotalPrice());
        transitionTo(payment, PaymentStatus.PAID);
        paymentRepository.save(payment);

        return PaymentResponseDto.of(payment);
    }

    public PaymentResponseDto fail(PaymentStatusRequestDto dto, String email) {
        getOwnedOrder(dto.getOrderId(), email);
        Payment payment = getPaymentByOrderId(dto.getOrderId());

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException("결제 완료 상태는 실패로 변경할 수 없습니다");
        }

        transitionTo(payment, PaymentStatus.FAILED);
        return PaymentResponseDto.of(payment);
    }

    public PaymentResponseDto cancel(PaymentStatusRequestDto dto, String email) {
        getOwnedOrder(dto.getOrderId(), email);
        Payment payment = getPaymentByOrderId(dto.getOrderId());

        cancelPayment(payment, true);
        return PaymentResponseDto.of(payment);
    }

    public void cancelForOrderCancellation(Long orderId, String email) {
        getOwnedOrder(orderId, email);
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> cancelPayment(payment, false));
    }

    private void cancelPayment(Payment payment, boolean strictMode) {
        if (payment.getStatus() == PaymentStatus.FAILED) {
            if (strictMode) {
                throw new IllegalArgumentException("실패한 결제는 취소할 수 없습니다");
            }
            return;
        }

        if (payment.getStatus() == PaymentStatus.CANCEL) {
            return;
        }

        if (payment.getStatus() == PaymentStatus.PAID && StringUtils.hasText(payment.getImpUid())) {
            paymentGateway.cancel(payment.getImpUid(), payment.getAmount(), "USER_REQUEST");
        }

        transitionTo(payment, PaymentStatus.CANCEL);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getStatus(Long orderId, String email) {
        getOwnedOrder(orderId, email);
        return PaymentResponseDto.of(getPaymentByOrderId(orderId));
    }

    private Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다"));
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문 정보를 찾을 수 없습니다"));
    }

    private Order getOwnedOrder(Long orderId, String email) {
        Order order = getOrder(orderId);
        if (!order.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("해당 주문에 접근할 권한이 없습니다");
        }
        return order;
    }

    private void validateOrderPayable(Order order) {
        if (order.getStatus() == OrderStatus.CANCEL) {
            throw new IllegalArgumentException("취소된 주문은 결제 처리할 수 없습니다");
        }
    }

    private Payment createPayment(Order order) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalPrice());
        payment.setStatus(PaymentStatus.READY);
        return payment;
    }

    private void transitionTo(Payment payment, PaymentStatus next) {
        PaymentStatus current = payment.getStatus();

        if (current == null) {
            payment.setStatus(next);
            return;
        }

        if (current == next) {
            return;
        }

        if (!current.canTransitTo(next)) {
            throw new IllegalArgumentException("허용되지 않는 결제 상태 변경입니다: " + current + " -> " + next);
        }

        payment.setStatus(next);
        return;
    }
}
