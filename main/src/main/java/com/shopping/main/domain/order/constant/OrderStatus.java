package com.shopping.main.domain.order.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    ORDER("주문 완료"),
    CANCEL("주문 취소");

    private final String description;
}