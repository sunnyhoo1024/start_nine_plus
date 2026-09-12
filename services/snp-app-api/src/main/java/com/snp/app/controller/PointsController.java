package com.snp.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.app.security.AppAuthContext;
import com.snp.common.api.ApiResponse;
import com.snp.entity.PointsLedger;
import com.snp.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/app/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @GetMapping("/balance")
    public ApiResponse<Map<String, Long>> balance() {
        Long userId = AppAuthContext.requireUserId();
        return ApiResponse.ok(Map.of("balance", pointsService.getBalance(userId)));
    }

    @GetMapping("/ledger")
    public ApiResponse<IPage<PointsLedger>> ledger(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "20") long size) {
        Long userId = AppAuthContext.requireUserId();
        return ApiResponse.ok(pointsService.listLedger(userId, page, size));
    }
}
