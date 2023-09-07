package com.block.chain.vo.management;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 后台-首页统计-基础数据看板-今日兑换得到的枚数
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-04-04 17:10:16
 */
@Data
public class ExchangeManagementVO {

	/**
	 * 公链
	 */
	private  String net;

	/**
	 * 币种
	 */
	private String symbol;

	/**
	 * 数量
	 */
	private BigDecimal amount;
}
