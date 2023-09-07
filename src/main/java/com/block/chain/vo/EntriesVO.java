package com.block.chain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 
 * 统计 - 入账数量，提现数量统计 -VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-25 10:17:17
 */
@Data
public class EntriesVO {

	/**
	 * 主币统计数量
	 */
	private Map<String ,BigDecimal> primarysMap;
	/**
	 * 代币统计数量
	 */
	private Map<String ,BigDecimal> tokensMap;



}
