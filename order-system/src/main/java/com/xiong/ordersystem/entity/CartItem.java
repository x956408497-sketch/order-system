package com.xiong.ordersystem.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItem {
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    private Integer stock;
    private Integer productStatus;
    private LocalDateTime createTime;
}
