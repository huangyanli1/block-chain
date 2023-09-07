package com.block.chain.vo;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 
 * 拿取地址下余额对应VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class RecoverBalanceVO {
	/**
	 * 主币余额
	 */
	private String balance;

	/**
	 * 公链类型(ETH BTC TRX MATIC HECO BSC)
	 */
	private String net;

	/**
	 * 币种符号
	 */
	private String symbol;

	/**
	 * 地址
	 */
	private String address;

	/**
	 * 代币信息
	 */
	private List<BalanceTokensVO> token;


}
