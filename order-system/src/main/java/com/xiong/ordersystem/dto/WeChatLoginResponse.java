package com.xiong.ordersystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeChatLoginResponse {
    private String token;
    private Long userId;
    private String nickname;
    private String mode;
}
