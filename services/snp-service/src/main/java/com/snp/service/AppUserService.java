package com.snp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.entity.AppUser;

public interface AppUserService {
    AppUser findOrCreateByOpenid(String openid, String unionid);

    AppUser updateProfile(Long userId, String nickname, String avatarUrl);

    AppUser getById(Long id);

    IPage<AppUser> pageUsers(long page, long size, String keyword);
}
