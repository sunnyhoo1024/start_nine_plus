package com.snp.admin.controller;

import com.snp.admin.security.AdminAuthContext;
import com.snp.common.api.ApiResponse;
import com.snp.dto.AdminLoginResult;
import com.snp.entity.AdminUser;
import com.snp.mapper.AdminUserMapper;
import com.snp.service.AuthAdminService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthAdminService authAdminService;
    private final AdminUserMapper adminUserMapper;

    @PostMapping("/login")
    public ApiResponse<AdminLoginResult> login(@RequestBody @Validated LoginRequest req) {
        return ApiResponse.ok(authAdminService.login(req.getUsername(), req.getPassword()));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        Long adminId = AdminAuthContext.currentAdminId();
        AdminUser user = authAdminService.getById(adminId);
        List<String> permissions = adminUserMapper.selectPermissionCodes(adminId);
        List<String> roles = adminUserMapper.selectRoleCodes(adminId);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("roles", roles);
        data.put("permissions", permissions);
        return ApiResponse.ok(data);
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }
}
