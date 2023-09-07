package com.block.chain.vo.convert;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 跨链桥 - 调用exchange now跨链桥创建兑换交易 - 订单参数
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-02-13 17:10:16
 */
@Data
public class ExchangeTransactionRestltVO {
	/**
	 * 扣除的币的数量
	 */
	private BigDecimal fromAmount;
	/**
	 *   得到的币的数量
	 */
	private BigDecimal toAmount;
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
	 * 可以使用该Id在事务状态API端点获取事务状态
	 */
	private String id;
	/**
	 * 发出币所在公链
	 */
	private String fromNetwork;
	/**
	 * 得到币所在公链
	 */
	private String toNetwork;
	/**
	 * 加密校验参数
	 */
	private String sign;
	/**
	 * 业务线id
	 */
	private Integer businessId;
	/**
	 * 回调Url - 保留字段
	 */
	private String callbackUrl;
}
