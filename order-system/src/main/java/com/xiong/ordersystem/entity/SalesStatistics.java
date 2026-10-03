package com.xiong.ordersystem.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SalesStatistics {
    private Long id;
    private LocalDate statDate;
    private Integer orderCount;
    private Integer productCount;
    private BigDecimal salesAmount;
    private LocalDateTime createTime;
}
