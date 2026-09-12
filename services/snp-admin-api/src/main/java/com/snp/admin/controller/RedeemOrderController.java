package com.snp.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.common.api.ApiResponse;
import com.snp.entity.RedeemOrder;
import com.snp.service.RedeemService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/redeem-orders")
@RequiredArgsConstructor
public class RedeemOrderController {

    private final RedeemService redeemService;

    @GetMapping
    public ApiResponse<IPage<RedeemOrder>> page(@RequestParam(defaultValue = "1") long page,
                                                @RequestParam(defaultValue = "20") long size,
                                                @RequestParam(required = false) String status) {
        return ApiResponse.ok(redeemService.pageOrders(page, size, status));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<RedeemOrder> updateStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        return ApiResponse.ok(redeemService.updateOrderStatus(id, req.getStatus(), req.getRemark()));
    }

    @Data
    public static class StatusRequest {
        private String status;
        private String remark;
    }
}
