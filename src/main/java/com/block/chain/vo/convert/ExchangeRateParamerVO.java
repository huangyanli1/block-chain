package com.block.chain.vo.convert;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 币币兑换 - 币种转化汇率 - 参数VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-12-07 17:10:16
 */
@Data
public class ExchangeRateParamerVO {
	/**
	 * 入账交易币种符号
	 */
	private String entrySymbol;
	/**
	 * 入账交易币种数量
	 */
	private BigDecimal entryAmount;

	/**
	 * 出账交易币种符号
	 */
	private String outSymbol;

	/**
	 * 上一次汇率数据
	 */
	private BigDecimal oldExchangeRate;
}
