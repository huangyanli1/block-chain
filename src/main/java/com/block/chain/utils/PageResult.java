package com.block.chain.utils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 分页数据
 */
@Data
@ApiModel(value = "分页统一数据返回对象", description = "所有分页数据经此包装")
public class PageResult<T> {

    @ApiModelProperty(required = true, value = "当前页", dataType = "Long", example = "1", position = 0)
    private Long currentPage;

    @ApiModelProperty(required = true, value = "每页记录数", dataType = "Long", example = "1", position = 0)
    private Long pageSize;

    @ApiModelProperty(required = true, value = "总数", dataType = "Long", example = "99", position = 2)
    private Long count;



    @ApiModelProperty(required = true, value = "返回数据", dataType = "string", example = "data", position = 4)
    private List<T> data;

    @ApiModelProperty(required = true, value = "msg", dataType = "string", example = "success", position = 3)
    private String message;

    @ApiModelProperty(required = true, value = "总页数", dataType = "long", example = "1", position = 1)
    private Long pages;


}
