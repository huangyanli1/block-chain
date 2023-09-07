package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import lombok.Data;

/**
 * 公司地址存储表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-24 14:23:03
 */
@Data
@TableName("company_address")
public class CompanyAddressEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键Id
	 */
	@TableId
	private Long id;
	/**
	 * 公司地址
	 */
	private String address;
	/**
	 * 公链，目前值只有6个: 
BTC, ETH, MATIC, BSC, HECO, TRX
	 */
	private String net;
	/**
	 * 主币余额
	 */
	private BigDecimal balance;

}
