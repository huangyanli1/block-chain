package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
@TableName("quotation")
public class QuotationEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键Id
	 */
	@TableId
	private Long id;
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
