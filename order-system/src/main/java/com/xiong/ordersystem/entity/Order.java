package com.xiong.ordersystem.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private String receiverName;
    private String phone;
    private String detailAddress;
    private BigDecimal totalAmount;
    private Integer payStatus;
    private String orderStatus;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<OrderItem> items;
}
