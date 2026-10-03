package com.xiong.ordersystem.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CategorySalesStatistics {
    private Long categoryId;
    private String categoryName;
    private Integer salesCount;
    private BigDecimal salesAmount;
    private LocalDate latestStatDate;
}
