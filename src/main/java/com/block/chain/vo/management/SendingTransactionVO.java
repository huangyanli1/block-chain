package com.block.chain.vo.management;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 后台管理系统 - 加密货币交易管理 - 发送币列表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-28 17:10:16
 */
@Data
public class SendingTransactionVO {
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
	 * 燃油费 - 矿工费
	 */
	private BigDecimal transactionFee;


	/**
	 * 实际金额 =实发数量（diff） + 矿工费
	 */
	private BigDecimal actualAmount;

	/**
	 * 发送方地址:只有比特币家族的返回值里才可能是多个地址，其他的链都是一个或者没有。而且比特币家族可能from 和 to 都是同一个地址。
	 */
	private String fromAddr;
	/**
	 * 接收方地址
	 */
	private String toAddr;

	/**
	 * 1: received 服务端已经收到，但未广播
	 2: broadcasting 正在广播
	 3: unconfirmed 已经广播，尚未确认
	 4: confirmed 交易已经确认
	 5: 异常状态
	 */
	private Integer transactionStatus;

	/**
	 * 发送错误次数
	 */
	private Integer sendErrorNumber;
	/**
	 * 交易时间时间戳 - 交易时间
	 */
	private Long blockTime;

}
