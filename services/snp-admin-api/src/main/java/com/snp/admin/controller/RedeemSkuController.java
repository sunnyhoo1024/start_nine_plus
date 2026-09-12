package com.snp.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.common.api.ApiResponse;
import com.snp.entity.RedeemSku;
import com.snp.service.RedeemService;
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
@RequestMapping("/admin/api/redeem-skus")
@RequiredArgsConstructor
public class RedeemSkuController {

    private final RedeemService redeemService;

    @GetMapping
    public ApiResponse<IPage<RedeemSku>> page(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(redeemService.pageSkus(page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<RedeemSku> get(@PathVariable Long id) {
        return ApiResponse.ok(redeemService.getSku(id));
    }

    @PostMapping
    public ApiResponse<RedeemSku> create(@RequestBody RedeemSku sku) {
        sku.setId(null);
        return ApiResponse.ok(redeemService.saveSku(sku));
    }

    @PutMapping("/{id}")
    public ApiResponse<RedeemSku> update(@PathVariable Long id, @RequestBody RedeemSku sku) {
        sku.setId(id);
        return ApiResponse.ok(redeemService.saveSku(sku));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        redeemService.deleteSku(id);
        return ApiResponse.ok();
    }
}
