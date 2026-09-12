package com.snp.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.app.security.AppAuthContext;
import com.snp.common.api.ApiResponse;
import com.snp.entity.RedeemOrder;
import com.snp.entity.RedeemSku;
import com.snp.service.RedeemService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/app/api/redeem")
@RequiredArgsConstructor
@Validated
public class RedeemController {

    private final RedeemService redeemService;

    @GetMapping("/skus")
    public ApiResponse<List<RedeemSku>> listSkus() {
        return ApiResponse.ok(redeemService.listOnlineSkus());
    }

    @PostMapping
    public ApiResponse<RedeemOrder> redeem(@RequestBody @Validated RedeemRequest req) {
        Long userId = AppAuthContext.requireUserId();
        int qty = req.getQuantity() == null ? 1 : req.getQuantity();
        return ApiResponse.ok(redeemService.redeem(userId, req.getSkuId(), qty));
    }

    @GetMapping("/orders")
    public ApiResponse<IPage<RedeemOrder>> myOrders(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "20") long size) {
        Long userId = AppAuthContext.requireUserId();
        return ApiResponse.ok(redeemService.pageUserOrders(userId, page, size));
    }

    @Data
    public static class RedeemRequest {
        @NotNull
        private Long skuId;
        @Min(1)
        private Integer quantity = 1;
    }
}
