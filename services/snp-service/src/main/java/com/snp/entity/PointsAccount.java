package com.snp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_account")
public class PointsAccount {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long balance;
    @Version
    private Integer version;
    private LocalDateTime updatedAt;
}
