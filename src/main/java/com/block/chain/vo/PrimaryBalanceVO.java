package com.block.chain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 
 * 统计 - 总价值 -VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class PrimaryBalanceVO {
	/**
	 * 公链信息
	 */
	private String net;
	/**
	 * 余额
	 */
	private BigDecimal balance;

	private Map<String ,BigDecimal> tokensMap;

//	/**
//	 * 公链下代币信息和
//	 */
//	private List<TokenBalanceVO> tokenList;

}
