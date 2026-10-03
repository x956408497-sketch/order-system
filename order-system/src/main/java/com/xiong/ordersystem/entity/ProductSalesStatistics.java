package com.xiong.ordersystem.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductSalesStatistics {
    private Long productId;
    private String productName;
    private Integer salesCount;
    private BigDecimal salesAmount;
    private LocalDate latestStatDate;
}
