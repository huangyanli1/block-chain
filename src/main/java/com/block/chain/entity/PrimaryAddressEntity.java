package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 地址主表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-21 13:30:44
 */
@Data
@TableName("primary_address")
public class PrimaryAddressEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键Id
	 */
	@TableId
	private Long id;
	/**
	 * 地址
	 */
	private String address;
	/**
	 * ，目前值只有6个:
BTC, ETH, MATIC, BSC, HECO, TRX
	 */
	private String net;
	/**
	 * 主币余额
	 */
	private BigDecimal balance;
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
