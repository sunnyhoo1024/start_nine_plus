package com.snp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snp.common.exception.BizException;
import com.snp.entity.AppUser;
import com.snp.mapper.AppUserMapper;
import com.snp.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserMapper appUserMapper;

    @Override
    @Transactional
    public AppUser findOrCreateByOpenid(String openid, String unionid) {
        if (!StringUtils.hasText(openid)) {
            throw new BizException("openid不能为空");
        }
        AppUser user = appUserMapper.selectOne(new LambdaQueryWrapper<AppUser>()
                .eq(AppUser::getOpenid, openid));
        if (user != null) {
            if (StringUtils.hasText(unionid) && !unionid.equals(user.getUnionid())) {
                user.setUnionid(unionid);
                user.setUpdatedAt(LocalDateTime.now());
                appUserMapper.updateById(user);
            }
            return user;
        }
        user = new AppUser();
        user.setOpenid(openid);
        user.setUnionid(unionid);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        appUserMapper.insert(user);
        return user;
    }

    @Override
    public AppUser updateProfile(Long userId, String nickname, String avatarUrl) {
        AppUser user = getById(userId);
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatarUrl != null) {
            user.setAvatarUrl(avatarUrl);
        }
        user.setUpdatedAt(LocalDateTime.now());
        appUserMapper.updateById(user);
        return getById(userId);
    }

    @Override
    public AppUser getById(Long id) {
        AppUser user = appUserMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    @Override
    public IPage<AppUser> pageUsers(long page, long size, String keyword) {
        LambdaQueryWrapper<AppUser> wrapper = new LambdaQueryWrapper<AppUser>()
                .orderByDesc(AppUser::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AppUser::getNickname, keyword)
                    .or().like(AppUser::getOpenid, keyword));
        }
        return appUserMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
