package com.shopping.main.domain.order.dto;

import com.shopping.main.domain.order.entity.OrderItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {
    private String itemNm;
    private int count;
    private int orderPrice;
    private String imgUrl;

    public static OrderItemDto of(OrderItem orderItem, String imgUrl) {
        OrderItemDto dto = new OrderItemDto();
        dto.setItemNm(orderItem.getProduct().getName());
        dto.setCount(orderItem.getCount());
        dto.setOrderPrice(orderItem.getOrderPrice());
        dto.setImgUrl(imgUrl);
        return dto;
    }
}
