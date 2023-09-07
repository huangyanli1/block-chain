package com.block.chain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 法币汇率信息VO - 只针对美元使用
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-07-04 11:25:00
 */
@Data
public class ExchangeRateVO {
	/**
	 * 源货币
	 */
	private String source;
	/**
	 * 报价货币
	 */
	private String target;
	/**
	 * 汇率
	 */
	private BigDecimal rate;
	/**
	 * 修改时间
	 */
	private Long timestmp;
	/**
	 * 符号
	 */
	private String symbol;



}
