package com.block.chain.vo;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 
 * 代币余额信息VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class BalanceTokensVO {
	/**
	 * 代币名称
	 */
	private String symbol;
	/**
	 * 类型(ETH BTC TRX MATIC HECO BSC)
	 */
	private String net;
	private String standard;
	/**
	 * 地址
	 */
	private String address;
	/**
	 * 精度
	 */
	private Integer decimal;
	/**
	 * 代币余额
	 */
	private String balance;
	/**
	 * 币类型 1: 公链主币  2: ERC 20类代币  3: OMNI 类代币
	 */
	private Integer coinType;
}
