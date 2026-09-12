package com.snp.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.common.api.ApiResponse;
import com.snp.entity.CmsBanner;
import com.snp.service.CmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final CmsService cmsService;

    @GetMapping
    public ApiResponse<IPage<CmsBanner>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(cmsService.pageBanners(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<CmsBanner> get(@PathVariable Long id) {
        return ApiResponse.ok(cmsService.getBanner(id));
    }

    @PostMapping
    public ApiResponse<CmsBanner> create(@RequestBody CmsBanner banner) {
        banner.setId(null);
        return ApiResponse.ok(cmsService.saveBanner(banner));
    }

    @PutMapping("/{id}")
    public ApiResponse<CmsBanner> update(@PathVariable Long id, @RequestBody CmsBanner banner) {
        banner.setId(id);
        return ApiResponse.ok(cmsService.saveBanner(banner));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cmsService.deleteBanner(id);
        return ApiResponse.ok();
    }
}
