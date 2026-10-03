package com.xiong.ordersystem.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatisticsOverview {
    private Long orderCount;
    private Long productCount;
    private BigDecimal salesAmount;
    private String topProductName;
}
