package com.snp.dto;

import com.snp.entity.CmsBanner;
import com.snp.entity.CmsBlock;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HomeAggregate {
    private List<CmsBanner> banners;
    private CmsBlock intro;
    private Long balance;
}
