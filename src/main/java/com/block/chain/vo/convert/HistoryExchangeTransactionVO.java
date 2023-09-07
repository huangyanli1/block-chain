package com.block.chain.vo.convert;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * exchange now跨链桥订单信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-15 11:55:29
 */
@Data
public class HistoryExchangeTransactionVO implements Serializable {
	private static final long serialVersionUID = 1L;

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
	/**
	 * 服务费(跨链桥收取)
	 */
	private BigDecimal serviceFeeAmount;
	/**
	 * 扣除的币的数量
	 */
	private String fromAmount;
	/**
	 * 得到的币的数量
	 */
	private String toAmount;
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
	 * 修改确认时间
	 */
	private Long updateTime;
	/**
	 * 订单创建时间
	 */
	private Long createTime;




	/**
	 * 发出币所在公链 对应的LogoUrl
	 */
	private String fromNetworkUrl;

	/**
	 * 发出币对应的LogoUrl - 扣除币
	 */
	private String fromCurrencyUrl;

	/**
	 * 得到币所在公链 - 对应的LogoUrl
	 */
	private String toNetworkUrl;

	/**
	 * 接收币对应的LogoUrl - 得到币
	 */
	private String toCurrencyUrl;

	/**
	 * 扣除币是否是该链的主币
	 */
	private Integer isMainCurrencyFrom;

	/**
	 * 得到币是否是该链的主币
	 */
	private Integer isMainCurrencyTo;

	/**
	 * 业务线id
	 */
	private Integer businessId;
	/**
	 * 回调Url - 保留字段
	 */
	private String callbackUrl;



}
