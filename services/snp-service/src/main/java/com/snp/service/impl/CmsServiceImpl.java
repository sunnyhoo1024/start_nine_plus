package com.snp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snp.common.enums.PublishStatus;
import com.snp.common.exception.BizException;
import com.snp.entity.CmsBanner;
import com.snp.entity.CmsBlock;
import com.snp.mapper.CmsBannerMapper;
import com.snp.mapper.CmsBlockMapper;
import com.snp.service.CmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CmsServiceImpl implements CmsService {

    private final CmsBannerMapper cmsBannerMapper;
    private final CmsBlockMapper cmsBlockMapper;

    @Override
    public IPage<CmsBanner> pageBanners(long page, long size) {
        return cmsBannerMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<CmsBanner>().orderByDesc(CmsBanner::getSortOrder).orderByDesc(CmsBanner::getId));
    }

    @Override
    public CmsBanner getBanner(Long id) {
        CmsBanner banner = cmsBannerMapper.selectById(id);
        if (banner == null) {
            throw new BizException("Banner不存在");
        }
        return banner;
    }

    @Override
    public CmsBanner saveBanner(CmsBanner banner) {
        LocalDateTime now = LocalDateTime.now();
        if (banner.getId() == null) {
            banner.setCreatedAt(now);
            banner.setUpdatedAt(now);
            if (banner.getSortOrder() == null) {
                banner.setSortOrder(0);
            }
            if (banner.getStatus() == null) {
                banner.setStatus(PublishStatus.DRAFT.getCode());
            }
            cmsBannerMapper.insert(banner);
        } else {
            banner.setUpdatedAt(now);
            cmsBannerMapper.updateById(banner);
        }
        return getBanner(banner.getId());
    }

    @Override
    public void deleteBanner(Long id) {
        cmsBannerMapper.deleteById(id);
    }

    @Override
    public IPage<CmsBlock> pageBlocks(long page, long size) {
        return cmsBlockMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<CmsBlock>().orderByDesc(CmsBlock::getId));
    }

    @Override
    public CmsBlock getBlock(Long id) {
        CmsBlock block = cmsBlockMapper.selectById(id);
        if (block == null) {
            throw new BizException("内容块不存在");
        }
        return block;
    }

    @Override
    public CmsBlock saveBlock(CmsBlock block) {
        LocalDateTime now = LocalDateTime.now();
        if (block.getId() == null) {
            if (!StringUtils.hasText(block.getCode())) {
                throw new BizException("code不能为空");
            }
            Long exists = cmsBlockMapper.selectCount(new LambdaQueryWrapper<CmsBlock>()
                    .eq(CmsBlock::getCode, block.getCode()));
            if (exists != null && exists > 0) {
                throw new BizException("code已存在");
            }
            block.setCreatedAt(now);
            block.setUpdatedAt(now);
            if (block.getStatus() == null) {
                block.setStatus(PublishStatus.DRAFT.getCode());
            }
            cmsBlockMapper.insert(block);
        } else {
            block.setUpdatedAt(now);
            cmsBlockMapper.updateById(block);
        }
        return getBlock(block.getId());
    }

    @Override
    public void deleteBlock(Long id) {
        cmsBlockMapper.deleteById(id);
    }

    @Override
    public List<CmsBanner> listPublishedBanners() {
        LocalDateTime now = LocalDateTime.now();
        return cmsBannerMapper.selectList(new LambdaQueryWrapper<CmsBanner>()
                .eq(CmsBanner::getStatus, PublishStatus.ONLINE.getCode())
                .and(w -> w.isNull(CmsBanner::getStartAt).or().le(CmsBanner::getStartAt, now))
                .and(w -> w.isNull(CmsBanner::getEndAt).or().ge(CmsBanner::getEndAt, now))
                .orderByDesc(CmsBanner::getSortOrder)
                .orderByDesc(CmsBanner::getId));
    }

    @Override
    public CmsBlock getPublishedBlockByCode(String code) {
        LocalDateTime now = LocalDateTime.now();
        return cmsBlockMapper.selectOne(new LambdaQueryWrapper<CmsBlock>()
                .eq(CmsBlock::getCode, code)
                .eq(CmsBlock::getStatus, PublishStatus.ONLINE.getCode())
                .and(w -> w.isNull(CmsBlock::getStartAt).or().le(CmsBlock::getStartAt, now))
                .and(w -> w.isNull(CmsBlock::getEndAt).or().ge(CmsBlock::getEndAt, now))
                .last("LIMIT 1"));
    }
}
