package com.block.chain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 公链下交易记录信息VO - 公链交易记录入库后查询VO
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-01 17:10:16
 */
@Data
public class AddressParameterVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 地址
	 */
	private String address;

	/**
	 * 公链类型
	 */
	private String net;
	/**
     * 需录入的表名
	 */
	private String tableName;
	/**
	 * 交易hash
	 */
	private String txHash;

	/**
	 * 币种符号
	 */
	private String symbol;

}
