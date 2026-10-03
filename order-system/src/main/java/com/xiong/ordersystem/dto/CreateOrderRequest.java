package com.xiong.ordersystem.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long addressId;
    private String remark;
}
