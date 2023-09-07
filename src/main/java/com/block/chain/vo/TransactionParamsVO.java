package com.block.chain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 
 * 公链交易记录查询参数 - VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class TransactionParamsVO {

	/**
	 * 查询类型 1、全部 2、划转 3、转账 4、收款
	 */
	private String type;
	/**
	 * 开始时间(时间戳)
	 */
	private Long startTime;

	/**
	 * 结束时间(时间戳)
	 */
	private Long endTime;

	/**
	 * 交易所在的区块的hash - 用来做分页锚点(用来处理分页时出现最新的交易情况)
	 */
	private String txHash;

	/**
	 * 页码
	 */
	private Integer page;

	/**
	 * 每页显示数量
	 */
	private Integer pageSize;


	/**
	 * 查询地址信息
	 */
	private List<TransactionAddressVO> addressList;


}
