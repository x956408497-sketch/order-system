package com.xiong.ordersystem.controller;

import com.xiong.ordersystem.dto.AdminAccountRequest;
import com.xiong.ordersystem.dto.AdminLoginRequest;
import com.xiong.ordersystem.dto.AdminLoginResponse;
import com.xiong.ordersystem.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public AdminLoginResponse login(@RequestBody AdminLoginRequest request) {
        return adminAuthService.login(request);
    }

    @PostMapping("/account")
    public void updateAccount(HttpServletRequest httpRequest, @RequestBody AdminAccountRequest request) {
        adminAuthService.updateAccount(httpRequest.getHeader("X-Admin-Token"), request);
    }
}
