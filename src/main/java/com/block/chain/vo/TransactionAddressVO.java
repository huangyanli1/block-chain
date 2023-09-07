package com.block.chain.vo;

import lombok.Data;

/**
 * 
 * 公链交易记录查询参数-地址相关 - VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class TransactionAddressVO {

	/**
     * 公链类型：如ETH，BTC等
	 */
	private String net;
	/**
	 * 地址
	 */
	private String address;

	/**
	 * 地址类型：1用户 2公司
	 */
	private String addressType;

	/**
	 * 合约地址
	 */
	private String contractAddress;

	/**
	 * 币种符号
	 */
	private String symbol;

}
