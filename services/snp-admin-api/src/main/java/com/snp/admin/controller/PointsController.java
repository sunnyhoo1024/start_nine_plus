package com.snp.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.admin.security.AdminAuthContext;
import com.snp.common.api.ApiResponse;
import com.snp.entity.PointsLedger;
import com.snp.service.PointsService;
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

@RestController
@RequestMapping("/admin/api/points")
@RequiredArgsConstructor
@Validated
public class PointsController {

    private final PointsService pointsService;

    @PostMapping("/adjust")
    public ApiResponse<PointsLedger> adjust(@RequestBody @Validated AdjustRequest req) {
        Long operatorId = AdminAuthContext.currentAdminId();
        return ApiResponse.ok(pointsService.adminAdjust(req.getUserId(), req.getDelta(), req.getRemark(), operatorId));
    }

    @GetMapping("/ledger")
    public ApiResponse<IPage<PointsLedger>> ledger(@RequestParam Long userId,
                                                   @RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(pointsService.listLedger(userId, page, size));
    }

    @Data
    public static class AdjustRequest {
        @NotNull
        private Long userId;
        @NotNull
        private Long delta;
        private String remark;
    }
}
