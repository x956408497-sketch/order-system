package com.xiong.ordersystem.controller;

import com.xiong.ordersystem.dto.CartItemRequest;
import com.xiong.ordersystem.entity.CartItem;
import com.xiong.ordersystem.service.CartService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public List<CartItem> findByUserId(@RequestParam Long userId) {
        return cartService.findByUserId(userId);
    }

    @PostMapping("/items")
    public List<CartItem> add(@RequestParam Long userId, @RequestBody CartItemRequest request) {
        return cartService.add(userId, request.getProductId(), request.getQuantity());
    }

    @PutMapping("/items/{id}")
    public List<CartItem> updateQuantity(@RequestParam Long userId,
                                         @PathVariable Long id,
                                         @RequestBody CartItemRequest request) {
        return cartService.updateQuantity(userId, id, request.getQuantity());
    }

    @DeleteMapping("/items/{id}")
    public List<CartItem> delete(@RequestParam Long userId, @PathVariable Long id) {
        return cartService.delete(userId, id);
    }

    @DeleteMapping
    public void clear(@RequestParam Long userId) {
        cartService.clear(userId);
    }
}
