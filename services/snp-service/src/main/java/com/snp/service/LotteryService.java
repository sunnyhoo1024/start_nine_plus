package com.snp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.dto.LotteryDrawResult;
import com.snp.dto.LotteryPrizeAppView;
import com.snp.entity.LotteryActivity;
import com.snp.entity.LotteryPrize;

import java.util.List;

public interface LotteryService {
    IPage<LotteryActivity> pageActivities(long page, long size);

    LotteryActivity getActivity(Long id);

    LotteryActivity saveActivity(LotteryActivity activity);

    void deleteActivity(Long id);

    List<LotteryPrize> listPrizes(Long activityId);

    LotteryPrize savePrize(LotteryPrize prize);

    void deletePrize(Long id);

    List<LotteryActivity> listOnlineActivities();

    LotteryActivity getOnlineActivity(Long id);

    List<LotteryPrizeAppView> listAppPrizes(Long activityId);

    LotteryDrawResult draw(Long userId, Long activityId);
}
