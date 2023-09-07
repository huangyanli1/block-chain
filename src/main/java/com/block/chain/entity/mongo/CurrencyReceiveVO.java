package com.block.chain.entity.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 用户地址币种统计信息VO -首页统计 - 总数据统计
 */
@Data
@Document(collection = "currency_receive")
public class CurrencyReceiveVO {

    /**
     * 主键标识,该属性的值会自动对应，mongodb的主键字段”_id“,如果该属性名就叫”id“,则该注解可以省略
     */
    @Id
    private String id;

    /**
     * 今日接收，发送数量(笔数)
     */
    private Integer quantityToday;

    /**
     * 历史接收，发送数量（笔数）
     */
    private Integer quantityHistorical;
    /**
     * 初始地址余额
     */
    private BigDecimal addressBalance;
    /**
     * 总价值
     */
    private BigDecimal totalValue;

    /**
     * 币种统计信息
     */
    private List<SymbolCurrencyVO> symbolCurrency;

    /**
     * 统计时间-对应年月日
     */
    private String createTime;
    /**
     * 标识时间 - 用于查询
     */
    private Long markTime;
    /**
     * 修改时间-对应年月日
     */
    private String updateTime;

    /**
     * 趋势类型 0.平等 1.上升 2.下降
     */
    private Integer trendType;
}