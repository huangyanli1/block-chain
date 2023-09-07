package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 币种价格
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-17 18:45:18
 */
@Data
@TableName("currency_price")
public class CurrencyPriceEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId
	private Long id;
	/**
	 * 币种名称
	 */
	private String currencyName;
	/**
	 * 币种符号
	 */
	private String currencySymbol;
	/**
	 * 价格(USD)
	 */
	private BigDecimal priceUsd;
	/**
	 * 交易量(USD)
	 */
	private BigDecimal transactionVolume;
	/**
	 * 时间戳(毫秒)
	 */
	private Long timeMs;
	/**
	 * 交易量(单位为当前币种)
	 */
	private BigDecimal transactionCurrency;
	/**
	 * 报告交易量(单位为当前币种)
	 */
	private BigDecimal transactionReport;
	/**
	 * 报告交易量(USD)
	 */
	private BigDecimal transactionReportUsd;
	/**
	 * 市值(USD)
	 */
	private BigDecimal marketValue;
	/**
	 * 24小时涨跌幅
	 */
	private BigDecimal riseFallDay;
	/**
	 * 24小时最高价
	 */
	private BigDecimal highestPriceDay;
	/**
	 * 24小时最低价
	 */
	private BigDecimal minimumPriceDay;
	/**
	 * 1周涨跌幅
	 */
	private BigDecimal riseFallWeek;
	/**
	 * 1周最高价
	 */
	private BigDecimal highestPriceWeek;
	/**
	 * 1周最低价
	 */
	private BigDecimal minimumPriceWeek;
	/**
	 * 1月涨跌幅
	 */
	private BigDecimal riseFallMonth;
	/**
	 * 1月最高价
	 */
	private BigDecimal highestPriceMonth;
	/**
	 * 1月最低价
	 */
	private BigDecimal minimumPriceMonth;
	/**
	 * 历史最高价
	 */
	private BigDecimal highestPriceHistory;
	/**
	 * 历史最低价
	 */
	private BigDecimal minimumPriceHistory;

}
