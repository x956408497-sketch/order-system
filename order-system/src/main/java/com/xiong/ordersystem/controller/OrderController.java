package com.xiong.ordersystem.controller;

import com.xiong.ordersystem.dto.CreateOrderRequest;
import com.xiong.ordersystem.entity.Order;
import com.xiong.ordersystem.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order create(@RequestParam Long userId, @RequestBody CreateOrderRequest request) {
        return orderService.create(userId, request.getAddressId(), request.getRemark());
    }

    @GetMapping
    public List<Order> findByUserId(@RequestParam Long userId) {
        return orderService.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public Order findById(@RequestParam Long userId, @PathVariable Long id) {
        return orderService.findById(userId, id);
    }

    @PostMapping("/{id}/pay")
    public Order mockPay(@RequestParam Long userId, @PathVariable Long id) {
        return orderService.mockPay(userId, id);
    }

    @PostMapping("/{id}/confirm-receipt")
    public Order confirmReceipt(@RequestParam Long userId, @PathVariable Long id) {
        return orderService.confirmReceipt(userId, id);
    }

    @PostMapping("/{id}/cancel")
    public Order cancel(@RequestParam Long userId, @PathVariable Long id) {
        return orderService.cancel(userId, id);
    }
}
