package com.block.chain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class AddressVO{
	/**
	 * 地址
	 */
	private String address;
	/**
	 * 业务线id.
	 0:用户在在Ulla生态外部发起的交易，即: 不是Ulla合作方或Ulla自有系统发起的交易
	 1:Ulla 钱包
	 2:货币加密支付
	 3:交易所
	 */
	private Integer businessId;
	/**
	 * 是否初始化 1 未初始化
	 */
	private Integer isInitialize;

}
