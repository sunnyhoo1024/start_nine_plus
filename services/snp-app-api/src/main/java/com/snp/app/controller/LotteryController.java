package com.snp.app.controller;

import com.snp.app.security.AppAuthContext;
import com.snp.common.api.ApiResponse;
import com.snp.dto.LotteryDrawResult;
import com.snp.dto.LotteryPrizeAppView;
import com.snp.entity.LotteryActivity;
import com.snp.service.LotteryService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app/api/lottery")
@RequiredArgsConstructor
@Validated
public class LotteryController {

    private final LotteryService lotteryService;

    @GetMapping("/activities")
    public ApiResponse<List<LotteryActivity>> listActivities() {
        return ApiResponse.ok(lotteryService.listOnlineActivities());
    }

    @GetMapping("/activities/{id}")
    public ApiResponse<Map<String, Object>> activityDetail(@PathVariable Long id) {
        LotteryActivity activity = lotteryService.getOnlineActivity(id);
        List<LotteryPrizeAppView> prizes = lotteryService.listAppPrizes(id);
        Map<String, Object> data = new HashMap<>();
        data.put("activity", activity);
        data.put("prizes", prizes);
        return ApiResponse.ok(data);
    }

    @PostMapping("/draw")
    public ApiResponse<LotteryDrawResult> draw(@RequestBody @Validated DrawRequest req) {
        Long userId = AppAuthContext.requireUserId();
        return ApiResponse.ok(lotteryService.draw(userId, req.getActivityId()));
    }

    @Data
    public static class DrawRequest {
        @NotNull
        private Long activityId;
    }
}
