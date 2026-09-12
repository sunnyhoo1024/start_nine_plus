package com.snp.app.controller;

import com.snp.common.api.ApiResponse;
import com.snp.common.util.JwtUtil;
import com.snp.dto.AppLoginResult;
import com.snp.entity.AppUser;
import com.snp.service.AppUserService;
import com.snp.service.PointsService;
import com.snp.service.WeChatMiniService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/app/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final WeChatMiniService weChatMiniService;
    private final AppUserService appUserService;
    private final PointsService pointsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ApiResponse<AppLoginResult> login(@RequestBody @Validated LoginRequest req) {
        Map<String, String> session = weChatMiniService.code2session(req.getCode());
        String openid = session.get("openid");
        String unionid = session.get("unionid");
        AppUser user = appUserService.findOrCreateByOpenid(openid, StringUtils.hasText(unionid) ? unionid : null);
        if (StringUtils.hasText(req.getNickname()) || StringUtils.hasText(req.getAvatarUrl())) {
            user = appUserService.updateProfile(user.getId(),
                    StringUtils.hasText(req.getNickname()) ? req.getNickname() : user.getNickname(),
                    StringUtils.hasText(req.getAvatarUrl()) ? req.getAvatarUrl() : user.getAvatarUrl());
        }
        pointsService.getOrCreateAccount(user.getId());
        String token = jwtUtil.createAppToken(user.getId(), user.getOpenid());
        return ApiResponse.ok(AppLoginResult.builder()
                .token(token)
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .balance(pointsService.getBalance(user.getId()))
                .build());
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String code;
        private String nickname;
        private String avatarUrl;
    }
}
