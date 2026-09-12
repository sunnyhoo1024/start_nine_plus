package com.snp.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snp.entity.AdminPermission;
import com.snp.entity.AdminRole;
import com.snp.entity.AdminRolePermission;
import com.snp.entity.AdminUser;
import com.snp.entity.AdminUserRole;
import com.snp.mapper.AdminPermissionMapper;
import com.snp.mapper.AdminRoleMapper;
import com.snp.mapper.AdminRolePermissionMapper;
import com.snp.mapper.AdminUserMapper;
import com.snp.mapper.AdminUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private static final List<String[]> DEFAULT_PERMISSIONS = List.of(
            new String[]{"cms:read", "内容查看"},
            new String[]{"cms:write", "内容编辑"},
            new String[]{"user:read", "用户查看"},
            new String[]{"points:adjust", "积分调账"},
            new String[]{"lottery:write", "抽奖配置"},
            new String[]{"redeem:write", "兑换配置"}
    );

    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminPermissionMapper adminPermissionMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminRolePermissionMapper adminRolePermissionMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensurePermissions();
        AdminRole role = ensureSuperAdminRole();
        bindAllPermissions(role.getId());
        ensureAdminUser(role.getId());
    }

    private void ensurePermissions() {
        for (String[] item : DEFAULT_PERMISSIONS) {
            AdminPermission existing = adminPermissionMapper.selectOne(new LambdaQueryWrapper<AdminPermission>()
                    .eq(AdminPermission::getCode, item[0]));
            if (existing == null) {
                AdminPermission p = new AdminPermission();
                p.setCode(item[0]);
                p.setName(item[1]);
                p.setCreatedAt(LocalDateTime.now());
                adminPermissionMapper.insert(p);
            }
        }
    }

    private AdminRole ensureSuperAdminRole() {
        AdminRole role = adminRoleMapper.selectOne(new LambdaQueryWrapper<AdminRole>()
                .eq(AdminRole::getCode, "SUPER_ADMIN"));
        if (role == null) {
            role = new AdminRole();
            role.setCode("SUPER_ADMIN");
            role.setName("超级管理员");
            role.setCreatedAt(LocalDateTime.now());
            adminRoleMapper.insert(role);
        }
        return role;
    }

    private void bindAllPermissions(Long roleId) {
        List<AdminPermission> permissions = adminPermissionMapper.selectList(null);
        for (AdminPermission p : permissions) {
            Long count = adminRolePermissionMapper.selectCount(new LambdaQueryWrapper<AdminRolePermission>()
                    .eq(AdminRolePermission::getRoleId, roleId)
                    .eq(AdminRolePermission::getPermissionId, p.getId()));
            if (count == null || count == 0) {
                AdminRolePermission rp = new AdminRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(p.getId());
                adminRolePermissionMapper.insert(rp);
            }
        }
    }

    private void ensureAdminUser(Long roleId) {
        AdminUser user = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, "admin"));
        if (user == null) {
            user = new AdminUser();
            user.setUsername("admin");
            user.setPasswordHash(passwordEncoder.encode("admin123"));
            user.setNickname("超级管理员");
            user.setStatus(1);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            adminUserMapper.insert(user);
            log.info("Created default admin user admin / admin123");
        }

        Long count = adminUserRoleMapper.selectCount(new LambdaQueryWrapper<AdminUserRole>()
                .eq(AdminUserRole::getUserId, user.getId())
                .eq(AdminUserRole::getRoleId, roleId));
        if (count == null || count == 0) {
            AdminUserRole ur = new AdminUserRole();
            ur.setUserId(user.getId());
            ur.setRoleId(roleId);
            adminUserRoleMapper.insert(ur);
        }
    }
}
