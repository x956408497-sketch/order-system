package com.xiong.ordersystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiong.ordersystem.dto.WeChatLoginRequest;
import com.xiong.ordersystem.dto.WeChatLoginResponse;
import com.xiong.ordersystem.entity.AppUser;
import com.xiong.ordersystem.mapper.AppUserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeChatAuthService {
    private final AppUserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Long> sessions = new ConcurrentHashMap<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${wechat.app-id:}")
    private String appId;
    @Value("${wechat.app-secret:}")
    private String appSecret;

    public WeChatAuthService(AppUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public WeChatLoginResponse login(WeChatLoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getCode())) {
            throw new IllegalArgumentException("微信登录 code 不能为空");
        }
        String openid = exchangeOpenid(request.getCode().trim());
        AppUser user = userMapper.findByOpenid(openid);
        if (user == null) {
            user = new AppUser();
            user.setOpenid(openid);
            user.setNickname("微信用户");
            userMapper.insert(user);
            user = userMapper.findByOpenid(openid);
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getId());
        return new WeChatLoginResponse(token, user.getId(), user.getNickname(), isConfigured() ? "wechat" : "local");
    }

    public Long userId(String token) {
        return StringUtils.hasText(token) ? sessions.get(token) : null;
    }

    private String exchangeOpenid(String code) {
        if (!isConfigured()) {
            if ("local-demo".equals(code)) return "test-openid-001";
            throw new IllegalArgumentException("未配置微信 AppID/AppSecret，当前仅支持 code=local-demo 本地演示");
        }
        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid="
                    + encode(appId) + "&secret=" + encode(appSecret) + "&js_code=" + encode(code)
                    + "&grant_type=authorization_code";
            HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
            JsonNode json = objectMapper.readTree(response.body());
            if (!json.hasNonNull("openid")) throw new IllegalArgumentException("微信登录失败：" + json.path("errmsg").asText("未知错误"));
            return json.get("openid").asText();
        } catch (Exception exception) {
            if (exception instanceof IllegalArgumentException) throw (IllegalArgumentException) exception;
            throw new IllegalStateException("微信登录服务暂时不可用", exception);
        }
    }

    private boolean isConfigured() { return StringUtils.hasText(appId) && StringUtils.hasText(appSecret); }
    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
}
