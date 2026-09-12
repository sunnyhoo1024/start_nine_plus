package com.snp.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.snp.entity.RedeemOrder;
import com.snp.entity.RedeemSku;

import java.util.List;

public interface RedeemService {
    IPage<RedeemSku> pageSkus(long page, long size);

    RedeemSku getSku(Long id);

    RedeemSku saveSku(RedeemSku sku);

    void deleteSku(Long id);

    List<RedeemSku> listOnlineSkus();

    RedeemOrder redeem(Long userId, Long skuId, int quantity);

    RedeemOrder updateOrderStatus(Long orderId, String status, String remark);

    IPage<RedeemOrder> pageUserOrders(Long userId, long page, long size);

    IPage<RedeemOrder> pageOrders(long page, long size, String status);
}
