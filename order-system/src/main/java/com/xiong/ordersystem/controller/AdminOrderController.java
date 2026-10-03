package com.xiong.ordersystem.controller;

import com.xiong.ordersystem.entity.Order;
import com.xiong.ordersystem.service.AdminOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public List<Order> findAll() {
        return adminOrderService.findAll();
    }

    @GetMapping("/{id}")
    public Order findById(@PathVariable Long id) {
        return adminOrderService.findById(id);
    }

    @PostMapping("/{id}/to-ship")
    public Order markToShip(@PathVariable Long id) {
        return adminOrderService.markToShip(id);
    }

    @PostMapping("/{id}/ship")
    public Order ship(@PathVariable Long id) {
        return adminOrderService.ship(id);
    }

    @PostMapping("/{id}/complete")
    public Order complete(@PathVariable Long id) {
        return adminOrderService.complete(id);
    }

    @PostMapping("/{id}/cancel")
    public Order cancel(@PathVariable Long id) {
        return adminOrderService.cancel(id);
    }
}
