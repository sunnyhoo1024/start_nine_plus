package com.snp.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.common.api.ApiResponse;
import com.snp.entity.CmsBlock;
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
@RequestMapping("/admin/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final CmsService cmsService;

    @GetMapping
    public ApiResponse<IPage<CmsBlock>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(cmsService.pageBlocks(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<CmsBlock> get(@PathVariable Long id) {
        return ApiResponse.ok(cmsService.getBlock(id));
    }

    @PostMapping
    public ApiResponse<CmsBlock> create(@RequestBody CmsBlock block) {
        block.setId(null);
        return ApiResponse.ok(cmsService.saveBlock(block));
    }

    @PutMapping("/{id}")
    public ApiResponse<CmsBlock> update(@PathVariable Long id, @RequestBody CmsBlock block) {
        block.setId(id);
        return ApiResponse.ok(cmsService.saveBlock(block));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cmsService.deleteBlock(id);
        return ApiResponse.ok();
    }
}
