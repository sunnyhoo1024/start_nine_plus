package com.snp.app.controller;

import com.snp.app.security.AppAuthContext;
import com.snp.common.api.ApiResponse;
import com.snp.entity.AppUser;
import com.snp.service.AppUserService;
import com.snp.service.PointsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/app/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AppUserService appUserService;
    private final PointsService pointsService;

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        Long userId = AppAuthContext.requireUserId();
        AppUser user = appUserService.getById(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("nickname", user.getNickname());
        data.put("avatarUrl", user.getAvatarUrl());
        data.put("balance", pointsService.getBalance(userId));
        return ApiResponse.ok(data);
    }

    @PutMapping
    public ApiResponse<AppUser> update(@RequestBody ProfileRequest req) {
        Long userId = AppAuthContext.requireUserId();
        return ApiResponse.ok(appUserService.updateProfile(userId, req.getNickname(), req.getAvatarUrl()));
    }

    @Data
    public static class ProfileRequest {
        private String nickname;
        private String avatarUrl;
    }
}
