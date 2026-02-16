package com.shopping.main.domain.order.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.shopping.main.domain.order.dto.OrderDto;
import com.shopping.main.domain.order.dto.OrderHistDto;
import com.shopping.main.domain.order.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Order", description = "주문 API")
@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderServie;

    @Operation(summary = "상품 주문")
    @PostMapping("/orders")
    public @ResponseBody ResponseEntity<?> order(@RequestBody @Valid OrderDto orderDto, Principal principal) {

        // 1. 로그인 여부 검사
        if (principal == null) {
            return new ResponseEntity<String>("로그인이 필요합니다", HttpStatus.UNAUTHORIZED);
        }

        // 2. 주문 로직 호출
        String email = principal.getName();
        Long orderId = orderServie.order(orderDto, email);

        // 3. 주문 성공 시
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

    @GetMapping(value = { "/orders", "/orders/{page}" })
    public String orderHist(@PathVariable(value = "page") Optional<Integer> page, Principal principal, Model model) {

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 4);

        Page<OrderHistDto> orderHistDtoList = orderServie.getOrderList(principal.getName(), pageable);

        model.addAttribute("orders", orderHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", 5);

        return "order/orderHist";
    }

    @Operation(summary = "주문 취소")
    @PostMapping("/orders/{orderId}/cancel")
    public @ResponseBody ResponseEntity<?> cancleOrder(@PathVariable("orderId") Long orderId, Principal principal) {

        if (!orderServie.validateOrder(orderId, principal.getName())) {
            return new ResponseEntity<String>("취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        orderServie.cancelOrder(orderId);

        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }
}
