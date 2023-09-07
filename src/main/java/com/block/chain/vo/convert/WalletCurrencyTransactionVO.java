package com.block.chain.vo.convert;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 币币兑换交易信息 - 对去中心化交易所
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-12-10 11:59:26
 */
@Data
public class WalletCurrencyTransactionVO {
	/**
	 * 订单Id(guid实现 - 对外暴露)
	 */
	private String orderId;
	/**
	 * 业务线id.
0: 用户在在Ulla生态外部发起的交易，即: 不是Ulla合作方或Ulla自有系统发起的交易
1: Ulla 钱包
2:货币加密支付
	 */
	private Integer businessId;
	/**
	 * 订单Id(去中心化交易所订单Id)
	 */
	private String dexOrderId;
	/**
	 * 去中心化交易所Id - 来源
	 */
	private String dexId;
	/**
	 * 交易hash 
	 */
	private String txHash;
	/**
	 * 交易人出币地址 - 入账币地址
	 */
	private String entryAddress;
	/**
	 * 入账币- 公链类型(ETH BTC TRX MATIC HECO BSC)
	 */
	private String entryNet;
	/**
	 * 入账币 - 币种符号
	 */
	private String entrySymbol;
	/**
	 * 交易时入账币价格
	 */
	private BigDecimal entryPrice;
	/**
	 * 入账币数量
	 */
	private BigDecimal entryAmount;
	/**
	 * 出账币- 公链类型(ETH BTC TRX MATIC HECO BSC)
	 */
	private String outNet;
	/**
	 * 出账币-币种符号
	 */
	private String outSymbol;
	/**
	 * 交易时出账币价格
	 */
	private BigDecimal outPrice;
	/**
	 * 出账币数量
	 */
	private String outAmount;
	/**
	 * 交易状态2: broadcasting 正在广播
	 * 3: unconfirmed 已经广播，尚未确认
	 * 4: confirmed 交易已经确认
	 */
	private Integer transactionStatus;
	/**
	 * 交易发起的时间
13位毫秒时间戳
	 */
	private Long createTime;
	/**
	 * 订单修改时间 - 13位毫秒时间戳
	 */
	private Long updateTime;

	/**
	 * 业务数据 - 透传信息- 不做修改
	 */
	private String businessData;

	/**
	 * 服务费(公司收取)
	 */
	private BigDecimal serviceFeeAmount;
	/**
	 * 矿工费
	 */
	private BigDecimal gasFee;

}
