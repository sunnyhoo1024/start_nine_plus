package com.snp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.entity.CmsBanner;
import com.snp.entity.CmsBlock;

import java.util.List;

public interface CmsService {
    IPage<CmsBanner> pageBanners(long page, long size);

    CmsBanner getBanner(Long id);

    CmsBanner saveBanner(CmsBanner banner);

    void deleteBanner(Long id);

    IPage<CmsBlock> pageBlocks(long page, long size);

    CmsBlock getBlock(Long id);

    CmsBlock saveBlock(CmsBlock block);

    void deleteBlock(Long id);

    List<CmsBanner> listPublishedBanners();

    CmsBlock getPublishedBlockByCode(String code);
}
