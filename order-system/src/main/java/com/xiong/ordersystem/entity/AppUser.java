package com.xiong.ordersystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppUser {
    private Long id;
    private String openid;
    private String nickname;
    private String avatar;
    private String phone;
    private LocalDateTime createTime;
}
