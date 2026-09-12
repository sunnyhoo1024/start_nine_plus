package com.snp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_ledger")
public class PointsLedger {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long changeAmount;
    private Long balanceAfter;
    private String bizType;
    private String bizId;
    private String remark;
    private Long operatorId;
    private LocalDateTime createdAt;
}
