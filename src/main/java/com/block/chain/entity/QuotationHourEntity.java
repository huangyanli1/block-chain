package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 所有币种的小时K线
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-16 18:00:41
 */
@Data
@TableName("quotation_hour")
public class QuotationHourEntity implements Serializable {
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
	 * 最高价
	 */
	private BigDecimal highestPrice;
	/**
	 * 交易量
	 */
	private BigDecimal transactionVolume;
	/**
	 * 币种符号
	 */
	private String symbol;

}
