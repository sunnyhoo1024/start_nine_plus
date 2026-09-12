package com.snp.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.common.api.ApiResponse;
import com.snp.entity.LotteryActivity;
import com.snp.entity.LotteryPrize;
import com.snp.service.LotteryService;
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

import java.util.List;

@RestController
@RequestMapping("/admin/api/lottery")
@RequiredArgsConstructor
public class LotteryController {

    private final LotteryService lotteryService;

    @GetMapping("/activities")
    public ApiResponse<IPage<LotteryActivity>> pageActivities(@RequestParam(defaultValue = "1") long page,
                                                              @RequestParam(defaultValue = "20") long size) {
        return ApiResponse.ok(lotteryService.pageActivities(page, size));
    }

    @GetMapping("/activities/{id}")
    public ApiResponse<LotteryActivity> getActivity(@PathVariable Long id) {
        return ApiResponse.ok(lotteryService.getActivity(id));
    }

    @PostMapping("/activities")
    public ApiResponse<LotteryActivity> createActivity(@RequestBody LotteryActivity activity) {
        activity.setId(null);
        return ApiResponse.ok(lotteryService.saveActivity(activity));
    }

    @PutMapping("/activities/{id}")
    public ApiResponse<LotteryActivity> updateActivity(@PathVariable Long id, @RequestBody LotteryActivity activity) {
        activity.setId(id);
        return ApiResponse.ok(lotteryService.saveActivity(activity));
    }

    @DeleteMapping("/activities/{id}")
    public ApiResponse<Void> deleteActivity(@PathVariable Long id) {
        lotteryService.deleteActivity(id);
        return ApiResponse.ok();
    }

    @GetMapping("/activities/{activityId}/prizes")
    public ApiResponse<List<LotteryPrize>> listPrizes(@PathVariable Long activityId) {
        return ApiResponse.ok(lotteryService.listPrizes(activityId));
    }

    @PostMapping("/prizes")
    public ApiResponse<LotteryPrize> createPrize(@RequestBody LotteryPrize prize) {
        prize.setId(null);
        return ApiResponse.ok(lotteryService.savePrize(prize));
    }

    @PutMapping("/prizes/{id}")
    public ApiResponse<LotteryPrize> updatePrize(@PathVariable Long id, @RequestBody LotteryPrize prize) {
        prize.setId(id);
        return ApiResponse.ok(lotteryService.savePrize(prize));
    }

    @DeleteMapping("/prizes/{id}")
    public ApiResponse<Void> deletePrize(@PathVariable Long id) {
        lotteryService.deletePrize(id);
        return ApiResponse.ok();
    }
}
