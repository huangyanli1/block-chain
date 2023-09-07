package com.block.chain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 公链下交易记录信息VO - 主要用于公链交易记录录入
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-27 17:10:16
 */
@Data
public class TransactionHashInfoVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键Id
	 */
	private Long id;
	/**
	 * 交易所在的区块的高度
	 */
	private Integer blockHeight;
	/**
	 * 交易时间时间戳
	 */
	private Long blockTime;
	/**
	 * 交易hash 
	 */
	private String txHash;
	/**
	 * 金额：正数表示收入，负数表示支出
	 */
	private BigDecimal diff;
	/**
	 * 燃油费
	 */
	private BigDecimal transactionFee;
	/**
	 * 合约地址
	 */
	private String contractAddress;
	/**
	 * 币类型 1: 公链主币  2: ERC 20类代币  3: OMNI 类代币
	 */
	private Integer coinType;
	/**
	 * 发送方地址:只有比特币家族的返回值里才可能是多个地址，其他的链都是一个或者没有。而且比特币家族可能from 和 to 都是同一个地址。
	 */
	private List<String> fromAddr;
	/**
	 * 接收方地址
	 */
	private List<String> toAddr;
	/**
	 * 1: received 服务端已经收到，但未广播
       2: broadcasting 正在广播
       3: unconfirmed 已经广播，尚未确认
       4: confirmed 交易已经确认
       5: 异常状态
	 */
	private Integer transactionStatus;
	/**
	 * 确认时间时间戳
	 */
	private Long confirmTime;
	/**
	 * 合约输入参数
	 */
	private String contractInput;
	/**
	 * 合约输出参数
	 */
	private String contractOutput;
	/**
	 * 一次交易中gas的可用上限
	 */
	private BigDecimal gasLimit;
	/**
	 * 一个单位的gas价格
	 */
	private BigDecimal gasPrice;
	/**
	 * 交易所在的区块的hash
	 */
	private String blockHash;
	/**
	 * BTC input信息
	 */
	private String utxoInputs;
	/**
	 * BTC output信息
	 */
	private String utxoOutputs;
	/**
	 * BTC 所有者地址
	 */
	private String ownerAddress;
	/**
	 * 交易时币价格
	 */
	private BigDecimal coinPrice;

	/**
	 * 交易总价值(交易币数量乘以币价格)
	 */
	private BigDecimal coinValue;

	/**
	 * 公链类型
	 */
	private String net;
	/**
	 * 交易币种符号
	 */
	private String symbol;
}
