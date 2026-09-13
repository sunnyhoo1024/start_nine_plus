package com.snp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.snp.common.jackson.FlexibleJsonStringDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cms_block")
public class CmsBlock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String title;
    /** DB stores JSON text; API accepts either a JSON string or object. */
    @JsonDeserialize(using = FlexibleJsonStringDeserializer.class)
    private String contentJson;
    private Integer status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
