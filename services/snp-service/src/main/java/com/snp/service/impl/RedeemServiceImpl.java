package com.snp.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.snp.common.api.ErrorCode;
import com.snp.common.enums.PointsBizType;
import com.snp.common.enums.PublishStatus;
import com.snp.common.enums.RedeemOrderStatus;
import com.snp.common.exception.BizException;
import com.snp.entity.RedeemOrder;
import com.snp.entity.RedeemSku;
import com.snp.mapper.RedeemOrderMapper;
import com.snp.mapper.RedeemSkuMapper;
import com.snp.service.PointsService;
import com.snp.service.RedeemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RedeemServiceImpl implements RedeemService {

    private final RedeemSkuMapper redeemSkuMapper;
    private final RedeemOrderMapper redeemOrderMapper;
    private final PointsService pointsService;

    @Override
    public IPage<RedeemSku> pageSkus(long page, long size) {
        return redeemSkuMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<RedeemSku>().orderByDesc(RedeemSku::getId));
    }

    @Override
    public RedeemSku getSku(Long id) {
        RedeemSku sku = redeemSkuMapper.selectById(id);
        if (sku == null) {
            throw new BizException("兑换商品不存在");
        }
        return sku;
    }

    @Override
    public RedeemSku saveSku(RedeemSku sku) {
        LocalDateTime now = LocalDateTime.now();
        if (sku.getId() == null) {
            sku.setCreatedAt(now);
            sku.setUpdatedAt(now);
            if (sku.getStock() == null) {
                sku.setStock(0);
            }
            if (sku.getStatus() == null) {
                sku.setStatus(PublishStatus.DRAFT.getCode());
            }
            redeemSkuMapper.insert(sku);
        } else {
            sku.setUpdatedAt(now);
            redeemSkuMapper.updateById(sku);
        }
        return getSku(sku.getId());
    }

    @Override
    public void deleteSku(Long id) {
        redeemSkuMapper.deleteById(id);
    }

    @Override
    public List<RedeemSku> listOnlineSkus() {
        LocalDateTime now = LocalDateTime.now();
        return redeemSkuMapper.selectList(new LambdaQueryWrapper<RedeemSku>()
                .eq(RedeemSku::getStatus, PublishStatus.ONLINE.getCode())
                .and(w -> w.isNull(RedeemSku::getStartAt).or().le(RedeemSku::getStartAt, now))
                .and(w -> w.isNull(RedeemSku::getEndAt).or().ge(RedeemSku::getEndAt, now))
                .orderByDesc(RedeemSku::getId));
    }

    @Override
    @Transactional
    public RedeemOrder redeem(Long userId, Long skuId, int quantity) {
        if (quantity <= 0) {
            throw new BizException("兑换数量必须大于0");
        }
        RedeemSku sku = getSku(skuId);
        if (sku.getStatus() == null || sku.getStatus() != PublishStatus.ONLINE.getCode()) {
            throw new BizException(ErrorCode.REDEEM_UNAVAILABLE, "商品未上线");
        }
        LocalDateTime now = LocalDateTime.now();
        if (sku.getStartAt() != null && now.isBefore(sku.getStartAt())) {
            throw new BizException(ErrorCode.REDEEM_UNAVAILABLE, "兑换未开始");
        }
        if (sku.getEndAt() != null && now.isAfter(sku.getEndAt())) {
            throw new BizException(ErrorCode.REDEEM_UNAVAILABLE, "兑换已结束");
        }
        if (sku.getStock() == null || sku.getStock() < quantity) {
            throw new BizException(ErrorCode.STOCK_INSUFFICIENT);
        }

        long cost = sku.getPointsPrice() * quantity;
        int dec = redeemSkuMapper.decrementStock(skuId, quantity);
        if (dec == 0) {
            throw new BizException(ErrorCode.STOCK_INSUFFICIENT, "库存不足，请重试");
        }

        String orderNo = "R" + IdUtil.getSnowflakeNextIdStr();
        pointsService.changePoints(userId, -cost, PointsBizType.REDEEM, orderNo,
                "兑换:" + sku.getName() + " x" + quantity, null);

        RedeemOrder order = new RedeemOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setSkuId(skuId);
        order.setSkuName(sku.getName());
        order.setPointsCost(cost);
        order.setQuantity(quantity);
        order.setStatus(RedeemOrderStatus.CREATED.name());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        redeemOrderMapper.insert(order);
        return redeemOrderMapper.selectById(order.getId());
    }

    @Override
    @Transactional
    public RedeemOrder updateOrderStatus(Long orderId, String status, String remark) {
        RedeemOrder order = redeemOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        RedeemOrderStatus target;
        try {
            target = RedeemOrderStatus.valueOf(status);
        } catch (Exception e) {
            throw new BizException("无效状态");
        }
        if (RedeemOrderStatus.CANCELLED.name().equals(order.getStatus())) {
            throw new BizException("订单已取消");
        }
        if (RedeemOrderStatus.FULFILLED.name().equals(order.getStatus())
                && target != RedeemOrderStatus.FULFILLED) {
            throw new BizException("已履约订单不可变更");
        }

        if (target == RedeemOrderStatus.CANCELLED
                && !RedeemOrderStatus.CANCELLED.name().equals(order.getStatus())) {
            pointsService.changePoints(order.getUserId(), order.getPointsCost(),
                    PointsBizType.REDEEM_REFUND, order.getOrderNo(),
                    "兑换取消退还积分", null);
            redeemSkuMapper.incrementStock(order.getSkuId(), order.getQuantity());
        }

        order.setStatus(target.name());
        if (remark != null) {
            order.setRemark(remark);
        }
        order.setUpdatedAt(LocalDateTime.now());
        redeemOrderMapper.updateById(order);
        return redeemOrderMapper.selectById(orderId);
    }

    @Override
    public IPage<RedeemOrder> pageUserOrders(Long userId, long page, long size) {
        return redeemOrderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<RedeemOrder>()
                        .eq(RedeemOrder::getUserId, userId)
                        .orderByDesc(RedeemOrder::getId));
    }

    @Override
    public IPage<RedeemOrder> pageOrders(long page, long size, String status) {
        LambdaQueryWrapper<RedeemOrder> wrapper = new LambdaQueryWrapper<RedeemOrder>()
                .orderByDesc(RedeemOrder::getId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(RedeemOrder::getStatus, status);
        }
        return redeemOrderMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
