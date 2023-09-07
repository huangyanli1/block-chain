package com.block.chain.entity.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 公司钱包地址币种信息VO -首页统计 -symbol详情统计
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymbolCurrencyVO {
    /**
     * 公链信息
     */
    private String net;
    /**
     * 币种
     */
    private String symbol;

    /**
     * 今日接收，发送数量(笔数)
     */
    private Integer quantityToday;
    /**
     * 历史接收，发送数量（笔数）
     */
    private Integer quantityHistorical;

    /**
     * 历史接收,发送币数量
     */
    private BigDecimal historicalNumber;

    /**
     * 初始地址余额
     */
    private BigDecimal addressBalance;
    /**
     * 总价值(历史币数量乘以当时币种价格)
     */
    private BigDecimal totalValue;

    /**
     * 趋势类型 0.平等 1.上升 2.下降
     */
    private Integer trendType;



}