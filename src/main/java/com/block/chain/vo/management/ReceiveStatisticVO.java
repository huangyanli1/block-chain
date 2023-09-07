package com.block.chain.vo.management;

import com.block.chain.entity.mongo.SymbolCurrencyVO;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;


/**
 * 用于后台管理系统-首页统计接收统计-财务收款总额，笔数数据查询 - 时间段（日，周，月，年）
 */
@Data
public class ReceiveStatisticVO {

    /**
     * 主键标识,该属性的值会自动对应，mongodb的主键字段”_id“,如果该属性名就叫”id“,则该注解可以省略
     */
    private String id;

    /**
     * 今日接收，发送数量(笔数)
     */
    private Integer quantityToday;
    /**
     * 总价值
     */
    private BigDecimal totalValue;

    /**
     * 统计时间-对应年月日
     */
    private String createTime;
    /**
     * 标识时间 - 用于查询
     */
    private Long markTime;
}