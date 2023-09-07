package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * exchange now跨链桥订单信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-15 11:55:29
 */
@Data
@TableName("exchange_transaction")
public class ExchangeTransactionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId
	private Long id;
	/**
	 * 订单Id(guid实现 - 对外暴露)
	 */
	private String orderId;
	/**
	 * 跨链桥exchange now订单Id
	 */
	private String exchangeOrderId;
	/**
	 * 交易状态
	 */
	private Integer transactionStatus;
	/**R	 * 服务费(跨链桥收取)
	 */
	private BigDecimal serviceFeeAmount;
	/**
	 * 扣除的币的数量
	 */
	private BigDecimal fromAmount;
	/**
	 * 得到的币的数量
	 */
	private BigDecimal toAmount;
	/**
	 * 跨链桥收币交易矿工费
	 */
	private BigDecimal payinGasFee;
	/**
	 * 兑换所得的币交易矿工费
	 */
	private BigDecimal payoutGasFee;
	/**
	 * (可选）交换流类型。枚举：[“standard”，“fixed rate”]。默认值为标准值
	 */
	private String flow;
	/**
	 * (可选）交换流方向。枚举：[“direct”，“reverse”]。默认值为direct
	 */
	private String type;
	/**
	 * 支出币进入的地址-跨链桥收币地址
	 */
	private String payinAddress;
	/**
	 * 得到币进入地址 - 兑换所得的币指定的进入地址
	 */
	private String payoutAddress;
	/**
	 * 支付外部ID
	 */
	private String payoutExtraId;
	/**
	 * 发出币 - 扣除币
	 */
	private String fromCurrency;
	/**
	 * 接收币 - 得到币
	 */
	private String toCurrency;
	/**
	 * 退款地址
	 */
	private String refundAddress;
	/**
	 * 退款外部ID
	 */
	private String refundExtraId;
	/**
	 * 发出币所在公链
	 */
	private String fromNetwork;
	/**
	 * 得到币所在公链
	 */
	private String toNetwork;
	/**
	 * 跨链桥收币交易hash
	 */
	private String payinHash;
	/**
	 * 兑换所得的币交易hash
	 */
	private String payoutHash;
	/**
	 * 业务线id
	 */
	private Integer businessId;
	/**
	 * 回调Url - 保留字段
	 */
	private String callbackUrl;
	/**
	 * 修改确认时间
	 */
	private Long updateTime;
	/**
	 * 订单创建时间
	 */
	private Long createTime;

}
