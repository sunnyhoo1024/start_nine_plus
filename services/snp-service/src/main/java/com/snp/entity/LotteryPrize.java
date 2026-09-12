package com.snp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("lottery_prize")
public class LotteryPrize {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private String name;
    private String prizeType;
    private Long pointsValue;
    private Integer weight;
    private Integer stock;
    private String imageUrl;
    private Integer sortOrder;
    private Integer status;
}
