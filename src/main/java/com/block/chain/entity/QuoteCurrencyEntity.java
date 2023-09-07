package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 计价货币表 - 汇率引入时对应的计价货币
 * 
 * @author michael
 * @email 123456789@qq.com
 */
@Data
@TableName("quote_currency")
public class QuoteCurrencyEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId
	private Long id;
	/**
	 * 计价法币币种symbol
	 */
	private String symbol;
	/**
	 * 币种说明:{USD-USD,RUB-俄罗斯卢布（应该是俄语写的，跟切换语言的功能一样）}
	 */
	private String symbolExplain;
	/**
	 * 符号
	 */
	private String unit;
	/**
	 * 地区缩写
	 */
	private String acronym;
	/**
	 * 国家/地区
	 */
	private String nationExplain;

}
