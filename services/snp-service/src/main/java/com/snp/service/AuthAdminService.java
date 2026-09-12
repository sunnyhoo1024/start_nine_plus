package com.snp.service;

import com.snp.dto.AdminLoginResult;
import com.snp.entity.AdminUser;

public interface AuthAdminService {
    AdminLoginResult login(String username, String password);

    AdminUser getById(Long id);
}
