package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 公司地址下数据统计表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-29 13:14:32
 */
@Data
@TableName("statistic_address")
public class StatisticAddressEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键Id
	 */
	@TableId
	private Long id;
	/**
	 * 公链类型(ETH BTC TRX MATIC HECO BSC)
	 */
	private String net;
	/**
	 * 钱包地址
	 */
	private String address;
	/**
	 * 币个数
	 */
	private BigDecimal currencyNumber;
	/**
	 * 币种符号
	 */
	private String symbol;
	/**
	 * 币类型 1: 公链主币  2: ERC 20类代币  3: OMNI 类代币
	 */
	private Integer coinType;
	/**
	 * 今日入账交易笔数
	 */
	private Integer entriesNumber;
	/**
	 * 今日入账币数量
	 */
	private BigDecimal entriesCurrencyNumber;
	/**
	 * 今日提现交易笔数
	 */
	private Integer withdrawalNumber;
	/**
	 * 今日提现币数量
	 */
	private BigDecimal withdrawalCurrencyNumber;
	/**
	 * 创建时间
	 */
	private Date createTime;

}
