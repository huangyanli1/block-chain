package com.block.chain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 首页用户地址下所有币种统计数据返回
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-03 17:10:16
 */
@Data
public class UserBalanceVO implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 币种符号
	 */
	private String symbol;

	/**
	 * 币个数
	 */
	private String currencyNumber;

	/**
	 * 币种对应所有价值（币个数乘以币单价）
	 */
	private String value;

	/**
	 * 币种对应单价
	 */
	private String currencyPrice;

	/**
	 * 公链类型(ETH BTC TRX MATIC HECO BSC)
	 */
	private String net;
//
//	/**
//	 * 币类型 1: 公链主币  2: ERC 20类代币  3: OMNI 类代币
//	 */
//	private Integer coinType;

	/**
	 * 统计类型：1 币种统计 2 总价值统计
	 */
	private Integer statisticStatus;

}
