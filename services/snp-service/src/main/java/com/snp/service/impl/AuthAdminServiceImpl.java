package com.snp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snp.common.api.ErrorCode;
import com.snp.common.exception.BizException;
import com.snp.common.util.JwtUtil;
import com.snp.dto.AdminLoginResult;
import com.snp.entity.AdminUser;
import com.snp.mapper.AdminUserMapper;
import com.snp.service.AuthAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthAdminServiceImpl implements AuthAdminService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AdminLoginResult login(String username, String password) {
        AdminUser user = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username));
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(ErrorCode.AUTH_FAILED, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BizException(ErrorCode.AUTH_FAILED, "用户名或密码错误");
        }
        List<String> permissions = adminUserMapper.selectPermissionCodes(user.getId());
        List<String> roles = adminUserMapper.selectRoleCodes(user.getId());
        String token = jwtUtil.createAdminToken(user.getId(), user.getUsername(), permissions);
        return AdminLoginResult.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    public AdminUser getById(Long id) {
        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
            throw new BizException("管理员不存在");
        }
        return user;
    }
}
