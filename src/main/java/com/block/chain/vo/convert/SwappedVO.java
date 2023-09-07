package com.block.chain.vo.convert;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 1inch交换路由 - 拆解Logs数据交换的交易参数VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-24 17:10:16
 */
@Data
public class SwappedVO {

	//此次兑换交易时的发送地址
	private String sender;

	//此次兑换交易时扣除的代币合约地址
	private String srcToken;

	//此次兑换交易时得到的代币合约地址
	private String dstToken;

	//此次兑换交易时的接收地址
	private String dstReceiver;

	//兑换交易扣除的币的数量
	private Long spentAmount;

	//兑换交易得的币的数量
	private Long returnAmount;
}
