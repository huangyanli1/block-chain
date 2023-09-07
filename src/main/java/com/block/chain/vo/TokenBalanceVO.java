package com.block.chain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 
 * 统计 - 代币总价值 -VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class TokenBalanceVO {

	/**
	 * 名称
	 */
	private String name;
	/**
	 * 链信息
	 */
	private String net;
	/**
	 * 余额
	 */
	private BigDecimal balance;

}
