package com.block.chain.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 公司地址存储表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-24 14:23:03
 */
@Data
public class CompanyAddressVO {

	/**
	 * 主键Id
	 */
	private Long id;
	/**
	 * 公司地址
	 */
	private String address;
	/**
	 * 币种符号
	 */
	private String symbol;
	/**
	 * 公链，目前值只有6个: 
BTC, ETH, MATIC, BSC, HECO, TRX
	 */
	private String net;
	/**
	 * 主币余额
	 */
	private String balance;

	/**
	 * 实时价格($)
	 */
	private String price;

	/**
	 * 总价值($)
	 */
	private String totalValue;



}
