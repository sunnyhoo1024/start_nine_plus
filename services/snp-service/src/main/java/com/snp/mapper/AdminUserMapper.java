package com.snp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snp.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    @Select("""
            SELECT p.code FROM admin_permission p
            INNER JOIN admin_role_permission rp ON rp.permission_id = p.id
            INNER JOIN admin_user_role ur ON ur.role_id = rp.role_id
            WHERE ur.user_id = #{userId}
            """)
    List<String> selectPermissionCodes(@Param("userId") Long userId);

    @Select("""
            SELECT r.code FROM admin_role r
            INNER JOIN admin_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
            """)
    List<String> selectRoleCodes(@Param("userId") Long userId);
}
