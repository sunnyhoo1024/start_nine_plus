package com.snp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lottery_activity")
public class LotteryActivity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Long costPoints;
    private Integer dailyLimit;
    private Integer totalLimit;
    private Integer status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
