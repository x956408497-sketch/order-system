package com.xiong.ordersystem.controller;

import com.xiong.ordersystem.dto.WeChatLoginRequest;
import com.xiong.ordersystem.dto.WeChatLoginResponse;
import com.xiong.ordersystem.service.WeChatAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class WeChatAuthController {
    private final WeChatAuthService authService;

    public WeChatAuthController(WeChatAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/wechat-login")
    public WeChatLoginResponse login(@RequestBody WeChatLoginRequest request) {
        return authService.login(request);
    }
}
