package com.snp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin_user_role")
public class AdminUserRole {
    private Long userId;
    private Long roleId;
}
