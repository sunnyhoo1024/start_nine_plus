package com.snp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snp.common.enums.PointsBizType;
import com.snp.common.exception.BizException;
import com.snp.entity.PointsAccount;
import com.snp.entity.PointsLedger;
import com.snp.mapper.PointsAccountMapper;
import com.snp.mapper.PointsLedgerMapper;
import com.snp.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final PointsAccountMapper pointsAccountMapper;
    private final PointsLedgerMapper pointsLedgerMapper;

    @Override
    @Transactional
    public PointsAccount getOrCreateAccount(Long userId) {
        PointsAccount account = pointsAccountMapper.selectOne(new LambdaQueryWrapper<PointsAccount>()
                .eq(PointsAccount::getUserId, userId));
        if (account != null) {
            return account;
        }
        account = new PointsAccount();
        account.setUserId(userId);
        account.setBalance(0L);
        account.setVersion(0);
        account.setUpdatedAt(LocalDateTime.now());
        try {
            pointsAccountMapper.insert(account);
        } catch (Exception e) {
            account = pointsAccountMapper.selectOne(new LambdaQueryWrapper<PointsAccount>()
                    .eq(PointsAccount::getUserId, userId));
            if (account == null) {
                throw new BizException("创建积分账户失败");
            }
        }
        return account;
    }

    @Override
    public long getBalance(Long userId) {
        return getOrCreateAccount(userId).getBalance();
    }

    @Override
    @Transactional
    public PointsLedger changePoints(Long userId, long changeAmount, PointsBizType bizType,
                                     String bizId, String remark, Long operatorId) {
        if (changeAmount == 0) {
            throw new BizException("变动积分不能为0");
        }
        PointsAccount account = getOrCreateAccount(userId);
        long newBalance = account.getBalance() + changeAmount;
        if (newBalance < 0) {
            throw new BizException("积分不足");
        }
        account.setBalance(newBalance);
        account.setUpdatedAt(LocalDateTime.now());
        int updated = pointsAccountMapper.updateById(account);
        if (updated == 0) {
            throw new BizException("积分账户更新冲突，请重试");
        }

        PointsLedger ledger = new PointsLedger();
        ledger.setUserId(userId);
        ledger.setChangeAmount(changeAmount);
        ledger.setBalanceAfter(newBalance);
        ledger.setBizType(bizType.name());
        ledger.setBizId(bizId);
        ledger.setRemark(remark);
        ledger.setOperatorId(operatorId);
        ledger.setCreatedAt(LocalDateTime.now());
        pointsLedgerMapper.insert(ledger);
        return ledger;
    }

    @Override
    @Transactional
    public PointsLedger adminAdjust(Long userId, long delta, String remark, Long operatorId) {
        return changePoints(userId, delta, PointsBizType.ADMIN_ADJUST, null, remark, operatorId);
    }

    @Override
    public IPage<PointsLedger> listLedger(Long userId, long page, long size) {
        return pointsLedgerMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<PointsLedger>()
                        .eq(PointsLedger::getUserId, userId)
                        .orderByDesc(PointsLedger::getId));
    }
}
