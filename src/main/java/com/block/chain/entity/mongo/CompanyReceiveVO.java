package com.block.chain.entity.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;


/**
 * 公司钱包地址币种统计信息VO -首页统计 - 总数据统计
 */
@Data
@Document(collection = "company_receive")
public class CompanyReceiveVO {

    /**
     * 主键标识,该属性的值会自动对应，mongodb的主键字段”_id“,如果该属性名就叫”id“,则该注解可以省略
     */
    @Id
    private String id;

    /**
     * 历史接收，发送数量（笔数）
     */
    private Integer quantityHistorical;

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
    /**
     * 修改时间-对应年月日
     */
    private String updateTime;
}