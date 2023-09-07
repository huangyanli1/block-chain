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
public class ExchangeTransactionParamerVO {
    /**
    * 发出币 - 扣除币
    */
    private String fromCurrency;
   /**
    *  接收币 - 得到币
    */
    private String toCurrency;
	/**
	 * 发出币所在公链
	 */
	private String fromNetwork;
	/**
	 * 得到币所在公链
	 */
	private String toNetwork;
	/**
	 * 扣除的币的数量
	 */
	private String fromAmount;
	/**
	 * 得到的币的数量
	 */
	private String toAmount;
	/**
	 * 兑换得到币收币地址
	 */
	private String address;
	/**
	 * 外部ID
	 */
	private String extraId;
	/**
	 * 退款地址
	 */
	private String refundAddress;
	/**
	 * 退款外部ID
	 */
	private String refundExtraId;
	/**
	 *
	 */
	private String userId;
	/**
	 *
	 */
	private String payload;
	/**
	 *   合约
	 */
	private String contactEmail;
	/**
	 * 来源
	 */
	private String source;
	/**
	 * (可选）交换流类型。枚举：[“standard”，“fixed rate”]。默认值为标准值
	 */
	private String flow;
	/**
	 * (可选）交换流方向。枚举：[“direct”，“reverse”]。默认值为direct
	 */
	private String type;
	/**
	 * 固定流量需要RateId。若将参数“useRateId”设置为true，则可以在下一个创建交易的方法中使用返回的字段“rateId”来冻结在该方法中获得的估计金额。当前估计金额在字段“validUntil”中的时间之前有效
	 */
	private String rateId;

	/**
	 * 业务线id
	 */
	private Integer businessId;
	/**
	 * 回调Url - 保留字段
	 */
	private String callbackUrl;

}
