package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 法币汇率信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-06-06 11:25:00
 */
@Data
@TableName("currency_rate")
public class CurrencyRateEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId
	private Long id;
	/**
	 * 源货币
	 */
	private String sourceCurrency;
	/**
	 * 报价货币
	 */
	private String quoteCurrency;
	/**
	 * 汇率
	 */
	private BigDecimal rate;
	/**
	 * 修改时间
	 */
	private Long updateTime;
	/**
	 * 创建时间
	 */
	private Long createTime;

}
