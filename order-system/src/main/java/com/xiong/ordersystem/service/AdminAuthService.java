package com.xiong.ordersystem.service;

import com.xiong.ordersystem.dto.AdminAccountRequest;
import com.xiong.ordersystem.dto.AdminLoginRequest;
import com.xiong.ordersystem.dto.AdminLoginResponse;
import com.xiong.ordersystem.entity.Admin;
import com.xiong.ordersystem.mapper.AdminMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminAuthService {

    private final AdminMapper adminMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Map<String, Admin> sessions = new ConcurrentHashMap<>();

    public AdminAuthService(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("账号和密码不能为空");
        }
        Admin admin = adminMapper.findByUsername(request.getUsername().trim());
        if (admin == null || !matches(request.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        if (!isEncoded(admin.getPassword())) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            adminMapper.updatePassword(admin.getId(), encodedPassword);
            admin.setPassword(encodedPassword);
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, admin);
        return new AdminLoginResponse(token, admin.getUsername(), admin.getRole());
    }

    public boolean isValid(String token) {
        return StringUtils.hasText(token) && sessions.containsKey(token);
    }

    public void updateAccount(String token, AdminAccountRequest request) {
        Admin current = sessions.get(token);
        if (current == null) {
            throw new IllegalArgumentException("登录已失效，请重新登录");
        }
        if (request == null || !StringUtils.hasText(request.getCurrentPassword())
                || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("当前密码、新账号和新密码都不能为空");
        }
        Admin stored = adminMapper.findById(current.getId());
        if (stored == null || !matches(request.getCurrentPassword(), stored.getPassword())) {
            throw new IllegalArgumentException("当前密码错误");
        }
        String username = request.getUsername().trim();
        String password = request.getPassword().trim();
        if (username.length() < 3 || username.length() > 32 || password.length() < 6 || password.length() > 32) {
            throw new IllegalArgumentException("账号长度为3到32位，密码长度为6到32位");
        }
        Admin existing = adminMapper.findByUsername(username);
        if (existing != null && !existing.getId().equals(stored.getId())) {
            throw new IllegalArgumentException("该账号已存在");
        }
        adminMapper.updateAccount(stored.getId(), username, passwordEncoder.encode(password));
        sessions.clear();
    }

    private boolean matches(String raw, String stored) {
        return isEncoded(stored) ? passwordEncoder.matches(raw, stored) : stored.equals(raw);
    }

    private boolean isEncoded(String value) {
        return value != null && value.startsWith("$2");
    }
}
