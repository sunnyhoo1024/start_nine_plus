package com.snp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin_role_permission")
public class AdminRolePermission {
    private Long roleId;
    private Long permissionId;
}
