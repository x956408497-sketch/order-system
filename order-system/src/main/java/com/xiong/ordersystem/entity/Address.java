package com.xiong.ordersystem.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Address {
    private Long id;
    private Long userId;
    private String receiverName;
    private String phone;
    private String detailAddress;
    private Integer isDefault;
    private LocalDateTime createTime;
}
