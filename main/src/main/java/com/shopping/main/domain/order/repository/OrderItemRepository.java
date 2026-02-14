package com.shopping.main.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopping.main.domain.order.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
