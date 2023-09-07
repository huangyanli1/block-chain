package com.block.chain.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class QuotationVO {
	/**
	 * 主键Id
	 */
	private Long id;

	private String tableName;
	/**
	 * 时间戳
	 */
	private Long quotationTime;
	/**
	 * 开盘价
	 */
	private BigDecimal openingPrice;
	/**
	 * 收盘价
	 */
	private BigDecimal closingPrice;
	/**
	 * 最低价
	 */
	private BigDecimal minimumPrice;
	/**
	 * 
	 */
	private BigDecimal highestPrice;
	/**
	 * 交易量
	 */
	private BigDecimal transactionVolume;
	/**
	 * K线类型 (数据点间隔)[1m,5m,15m,30m,1h,6h,1d],默认5m
	 */
	private String intervalType;
	/**
	 * 交易所(来源)
	 */
	private String exchange;

}
