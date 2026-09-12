package com.snp.dto;

import lombok.Data;

@Data
public class LotteryPrizeAppView {
    private Long id;
    private Long activityId;
    private String name;
    private String prizeType;
    private Long pointsValue;
    private String imageUrl;
    private Integer sortOrder;
}
