package com.xiong.ordersystem.dto;

import lombok.Data;

@Data
public class AdminAccountRequest {
    private String currentPassword;
    private String username;
    private String password;
}
