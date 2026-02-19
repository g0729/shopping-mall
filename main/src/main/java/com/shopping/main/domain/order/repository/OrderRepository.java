package com.shopping.main.domain.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopping.main.domain.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
        @Query("select o from Order o " +
                        "where o.user.id = :userId " +
                        "order by o.orderDate desc, o.id desc")
        List<Order> findOrdersByUserId(@Param("userId") Long userId, Pageable pageable);

        @Query("select count(o.id) from Order o " +
                        "where o.user.id = :userId ")
        Long countOrderByUserId(@Param("userId") Long userId);

        // 주문 ID 목록으로 OrderItems + Product 한번에 조회
        @Query("select distinct o from Order o " +
                        "join fetch o.orderItems oi " +
                        "join fetch oi.product " +
                        "where o.id in :orderIds " +
                        "order by o.orderDate desc")
        List<Order> findOrdersWithItems(@Param("orderIds") List<Long> orderIds);

        // 단건 주문 조회
        @Query("select distinct o from Order o " +
                        "join fetch o.orderItems oi " +
                        "join fetch oi.product " +
                        "where o.id = :orderId")
        Optional<Order> findOrderWithItems(@Param("orderId") Long orderId);
}
