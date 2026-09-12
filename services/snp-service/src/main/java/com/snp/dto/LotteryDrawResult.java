package com.snp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LotteryDrawResult {
    private Long prizeId;
    private String prizeName;
    private String prizeType;
    private Long pointsValue;
}
