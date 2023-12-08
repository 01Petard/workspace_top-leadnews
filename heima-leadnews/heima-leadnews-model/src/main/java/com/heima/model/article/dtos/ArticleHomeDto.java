package com.heima.model.article.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ArticleHomeDto {

    // 最大时间
    @ApiModelProperty(value = "最大时间", required = true)
    Date maxBehotTime;
    // 最小时间
    @ApiModelProperty(value = "最小时间", required = true)
    Date minBehotTime;
    // 分页数量
    @ApiModelProperty(value = "分页数量（加载文章的数量）", required = true)
    Integer size;
    // 频道ID
    @ApiModelProperty(value = "频道id", required = true)
    String tag;
}