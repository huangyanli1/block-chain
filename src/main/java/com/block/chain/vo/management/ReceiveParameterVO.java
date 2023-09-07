package com.block.chain.vo.management;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 后台管理系统 - 加密货币交易管理 - 接收币列表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-28 17:10:16
 */
@Data
public class ReceiveParameterVO {
	/**
	 * 主键Id
	 */
	private Long id;
	/**
	 * 类型(ETH BTC TRX MATIC HECO BSC)
	 */
	private String net;
	/**
	 * 交易币种符号
	 */
	private String symbol;
	/**
	 * 金额：正数表示收入，负数表示支出 - 接收数量
	 */
	private BigDecimal diff;

	/**
	 * 接收方地址
	 */
	private String toAddr;
	/**
	 * 交易时间时间戳 - 交易时间
	 */
	private Long blockTime;

	/**
	 * 确认信息 - 广播块的数量
	 */
	public Integer confirmInfo;
	/**
	 * 是否完成
	 */
	public Integer isComplete;
	/**
	 * 交易hash
	 */
	private String txHash;

	/**
	 * 是否已到账
	 */
	public Integer isConfirm;

	/**
	 * 添加时间 - 确认块时间
	 */
	private Long ConfirmTime;

}
