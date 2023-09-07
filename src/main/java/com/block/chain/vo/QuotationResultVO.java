package com.block.chain.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 所有币种的小时K线
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-16 18:00:41
 */
@Data
public class QuotationResultVO {
	/**
	 * 查询参数时间戳
	 */
	private Long queryTime;

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
