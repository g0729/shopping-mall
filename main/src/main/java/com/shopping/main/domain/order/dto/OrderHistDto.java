package com.shopping.main.domain.order.dto;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.shopping.main.domain.order.constant.OrderStatus;
import com.shopping.main.domain.order.entity.Order;
import com.shopping.main.domain.payment.constant.PaymentStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderHistDto {
    private Long orderId;
    private String orderDate;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus; // 결제 상태

    private List<OrderItemDto> orderItemDtoList = new ArrayList<>();

    public static OrderHistDto of(Order order) {
        OrderHistDto dto = new OrderHistDto();
        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        dto.setOrderStatus(order.getStatus());
        return dto;
    }

    public void addOrderItemDto(OrderItemDto orderItemDto) {
        this.orderItemDtoList.add(orderItemDto);
    }

    // 총 주문 금액 계산
    public int getTotalPrice() {
        return orderItemDtoList.stream()
                .mapToInt(item -> item.getOrderPrice() * item.getCount())
                .sum();
    }

    // 결제 완료 여부 확인
    public boolean isPaid() {
        return paymentStatus == PaymentStatus.PAID;
    }
}
