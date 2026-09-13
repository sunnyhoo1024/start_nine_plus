package com.snp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snp.common.api.ErrorCode;
import com.snp.common.enums.PointsBizType;
import com.snp.common.enums.PrizeType;
import com.snp.common.enums.PublishStatus;
import com.snp.common.exception.BizException;
import com.snp.dto.LotteryDrawResult;
import com.snp.dto.LotteryPrizeAppView;
import com.snp.entity.LotteryActivity;
import com.snp.entity.LotteryPrize;
import com.snp.entity.LotteryRecord;
import com.snp.mapper.LotteryActivityMapper;
import com.snp.mapper.LotteryPrizeMapper;
import com.snp.mapper.LotteryRecordMapper;
import com.snp.service.LotteryService;
import com.snp.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LotteryServiceImpl implements LotteryService {

    private final LotteryActivityMapper lotteryActivityMapper;
    private final LotteryPrizeMapper lotteryPrizeMapper;
    private final LotteryRecordMapper lotteryRecordMapper;
    private final PointsService pointsService;

    @Override
    public IPage<LotteryActivity> pageActivities(long page, long size) {
        return lotteryActivityMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<LotteryActivity>().orderByDesc(LotteryActivity::getId));
    }

    @Override
    public LotteryActivity getActivity(Long id) {
        LotteryActivity activity = lotteryActivityMapper.selectById(id);
        if (activity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "活动不存在");
        }
        return activity;
    }

    @Override
    public LotteryActivity saveActivity(LotteryActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        if (activity.getId() == null) {
            activity.setCreatedAt(now);
            activity.setUpdatedAt(now);
            if (activity.getCostPoints() == null) {
                activity.setCostPoints(0L);
            }
            if (activity.getDailyLimit() == null) {
                activity.setDailyLimit(0);
            }
            if (activity.getTotalLimit() == null) {
                activity.setTotalLimit(0);
            }
            if (activity.getStatus() == null) {
                activity.setStatus(PublishStatus.DRAFT.getCode());
            }
            lotteryActivityMapper.insert(activity);
        } else {
            activity.setUpdatedAt(now);
            lotteryActivityMapper.updateById(activity);
        }
        return getActivity(activity.getId());
    }

    @Override
    public void deleteActivity(Long id) {
        lotteryActivityMapper.deleteById(id);
        lotteryPrizeMapper.delete(new LambdaQueryWrapper<LotteryPrize>()
                .eq(LotteryPrize::getActivityId, id));
    }

    @Override
    public List<LotteryPrize> listPrizes(Long activityId) {
        return lotteryPrizeMapper.selectList(new LambdaQueryWrapper<LotteryPrize>()
                .eq(LotteryPrize::getActivityId, activityId)
                .orderByDesc(LotteryPrize::getSortOrder)
                .orderByAsc(LotteryPrize::getId));
    }

    @Override
    public LotteryPrize savePrize(LotteryPrize prize) {
        if (prize.getActivityId() == null) {
            throw new BizException("activityId不能为空");
        }
        getActivity(prize.getActivityId());
        if (prize.getId() == null) {
            if (prize.getWeight() == null) {
                prize.setWeight(1);
            }
            if (prize.getStock() == null) {
                prize.setStock(-1);
            }
            if (prize.getPointsValue() == null) {
                prize.setPointsValue(0L);
            }
            if (prize.getSortOrder() == null) {
                prize.setSortOrder(0);
            }
            if (prize.getStatus() == null) {
                prize.setStatus(1);
            }
            lotteryPrizeMapper.insert(prize);
        } else {
            lotteryPrizeMapper.updateById(prize);
        }
        return lotteryPrizeMapper.selectById(prize.getId());
    }

    @Override
    public void deletePrize(Long id) {
        lotteryPrizeMapper.deleteById(id);
    }

    @Override
    public List<LotteryActivity> listOnlineActivities() {
        LocalDateTime now = LocalDateTime.now();
        return lotteryActivityMapper.selectList(new LambdaQueryWrapper<LotteryActivity>()
                .eq(LotteryActivity::getStatus, PublishStatus.ONLINE.getCode())
                .and(w -> w.isNull(LotteryActivity::getStartAt).or().le(LotteryActivity::getStartAt, now))
                .and(w -> w.isNull(LotteryActivity::getEndAt).or().ge(LotteryActivity::getEndAt, now))
                .orderByDesc(LotteryActivity::getId));
    }

    @Override
    public LotteryActivity getOnlineActivity(Long id) {
        LotteryActivity activity = getActivity(id);
        validateActivityOnline(activity);
        return activity;
    }

    @Override
    public List<LotteryPrizeAppView> listAppPrizes(Long activityId) {
        return listPrizes(activityId).stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .map(p -> {
                    LotteryPrizeAppView view = new LotteryPrizeAppView();
                    BeanUtils.copyProperties(p, view);
                    return view;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LotteryDrawResult draw(Long userId, Long activityId) {
        LotteryActivity activity = getActivity(activityId);
        validateActivityOnline(activity);
        checkLimits(userId, activity);

        long cost = activity.getCostPoints() == null ? 0L : activity.getCostPoints();
        if (cost > 0) {
            pointsService.changePoints(userId, -cost, PointsBizType.LOTTERY_COST,
                    String.valueOf(activityId), "抽奖消耗", null);
        }

        List<LotteryPrize> candidates = lotteryPrizeMapper.selectList(new LambdaQueryWrapper<LotteryPrize>()
                .eq(LotteryPrize::getActivityId, activityId)
                .eq(LotteryPrize::getStatus, 1)
                .and(w -> w.eq(LotteryPrize::getStock, -1).or().gt(LotteryPrize::getStock, 0)));
        if (candidates.isEmpty()) {
            throw new BizException(ErrorCode.LOTTERY_NO_PRIZE);
        }

        LotteryPrize prize = weightedRandom(candidates);
        if (prize.getStock() != null && prize.getStock() > 0) {
            int dec = lotteryPrizeMapper.decrementStock(prize.getId());
            if (dec == 0) {
                throw new BizException(ErrorCode.STOCK_INSUFFICIENT, "奖品库存不足，请重试");
            }
        }

        if (PrizeType.POINTS.name().equals(prize.getPrizeType())
                && prize.getPointsValue() != null && prize.getPointsValue() > 0) {
            pointsService.changePoints(userId, prize.getPointsValue(), PointsBizType.LOTTERY_REWARD,
                    String.valueOf(prize.getId()), "抽奖奖励:" + prize.getName(), null);
        }

        LotteryRecord record = new LotteryRecord();
        record.setActivityId(activityId);
        record.setUserId(userId);
        record.setPrizeId(prize.getId());
        record.setPrizeName(prize.getName());
        record.setPrizeType(prize.getPrizeType());
        record.setPointsValue(prize.getPointsValue() == null ? 0L : prize.getPointsValue());
        record.setCreatedAt(LocalDateTime.now());
        lotteryRecordMapper.insert(record);

        return LotteryDrawResult.builder()
                .prizeId(prize.getId())
                .prizeName(prize.getName())
                .prizeType(prize.getPrizeType())
                .pointsValue(record.getPointsValue())
                .build();
    }

    private void validateActivityOnline(LotteryActivity activity) {
        if (activity.getStatus() == null || activity.getStatus() != PublishStatus.ONLINE.getCode()) {
            throw new BizException(ErrorCode.ACTIVITY_UNAVAILABLE, "活动未上线");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartAt() != null && now.isBefore(activity.getStartAt())) {
            throw new BizException(ErrorCode.ACTIVITY_UNAVAILABLE, "活动未开始");
        }
        if (activity.getEndAt() != null && now.isAfter(activity.getEndAt())) {
            throw new BizException(ErrorCode.ACTIVITY_UNAVAILABLE, "活动已结束");
        }
    }

    private void checkLimits(Long userId, LotteryActivity activity) {
        if (activity.getTotalLimit() != null && activity.getTotalLimit() > 0) {
            Long total = lotteryRecordMapper.selectCount(new LambdaQueryWrapper<LotteryRecord>()
                    .eq(LotteryRecord::getUserId, userId)
                    .eq(LotteryRecord::getActivityId, activity.getId()));
            if (total != null && total >= activity.getTotalLimit()) {
                throw new BizException(ErrorCode.LOTTERY_LIMIT, "已达活动总抽奖次数上限");
            }
        }
        if (activity.getDailyLimit() != null && activity.getDailyLimit() > 0) {
            LocalDateTime start = LocalDate.now().atStartOfDay();
            LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
            Long daily = lotteryRecordMapper.selectCount(new LambdaQueryWrapper<LotteryRecord>()
                    .eq(LotteryRecord::getUserId, userId)
                    .eq(LotteryRecord::getActivityId, activity.getId())
                    .between(LotteryRecord::getCreatedAt, start, end));
            if (daily != null && daily >= activity.getDailyLimit()) {
                throw new BizException(ErrorCode.LOTTERY_LIMIT, "今日抽奖次数已用完");
            }
        }
    }

    private LotteryPrize weightedRandom(List<LotteryPrize> prizes) {
        List<LotteryPrize> pool = new ArrayList<>();
        int totalWeight = 0;
        for (LotteryPrize p : prizes) {
            int w = p.getWeight() == null || p.getWeight() <= 0 ? 1 : p.getWeight();
            totalWeight += w;
            pool.add(p);
        }
        int r = ThreadLocalRandom.current().nextInt(totalWeight);
        int cursor = 0;
        for (LotteryPrize p : pool) {
            int w = p.getWeight() == null || p.getWeight() <= 0 ? 1 : p.getWeight();
            cursor += w;
            if (r < cursor) {
                return p;
            }
        }
        return pool.get(pool.size() - 1);
    }
}
