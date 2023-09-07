package com.block.chain.vo.management;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 法币汇率信息表 - 对推送消息
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-07-11 11:25:00
 */
@Data
public class FiatRateVO {
	/**
	 * 源货币
	 */
	private String sourceCurrency;
	/**
	 * 报价货币
	 */
	private String quoteCurrency;
	/**
	 * 汇率
	 */
	private BigDecimal rate;


}
