package com.snp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.common.enums.PointsBizType;
import com.snp.entity.PointsAccount;
import com.snp.entity.PointsLedger;

public interface PointsService {
    PointsAccount getOrCreateAccount(Long userId);

    long getBalance(Long userId);

    PointsLedger changePoints(Long userId, long changeAmount, PointsBizType bizType,
                              String bizId, String remark, Long operatorId);

    PointsLedger adminAdjust(Long userId, long delta, String remark, Long operatorId);

    IPage<PointsLedger> listLedger(Long userId, long page, long size);
}
