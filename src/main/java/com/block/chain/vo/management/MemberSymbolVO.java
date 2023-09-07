package com.block.chain.vo.management;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 后台管理系统 - 会员管理 - 会员基础信息 - 加密数字资产管理 - 按net，address，symbol统计信息VO
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-28 17:10:16
 */
@Data
public class MemberSymbolVO {

	/**
	 * 交易币种符号
	 */
	private String symbol;

	/**
	 * symbol 对应的交易数据数量
	 */
	private Integer number;

	/**
	 * symbol 对应的交易数据币数量之和
	 */
	private BigDecimal diff;
}
