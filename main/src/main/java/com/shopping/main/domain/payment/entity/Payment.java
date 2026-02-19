package com.shopping.main.domain.payment.entity;

import com.shopping.main.domain.order.entity.Order;
import com.shopping.main.domain.payment.constant.PaymentStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(name = "idx_payment_imp_uid", columnList = "imp_uid", unique = true) // imp_uid 중복 방지 결제 검증
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    private String impUid;

    private String merchantUid;

    private int amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
