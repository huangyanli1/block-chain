package com.block.chain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 
 * 公链交易记录分页返回参数 - VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class TransactionPageVO {

	/**
	 * 总记录数
	 */
	private int totalCount;
	/**
	 * 每页记录数
	 */
	private int pageSize;
	/**
	 * 总页数
	 */
	private int totalPage;
	/**
	 * 当前页数
	 */
	private int currPage;

	//第一次查询的第一条数据hash，用来锚点
	private String txHash;
	/**
	 * 列表数据
	 */
	private List<TransactionChainVO> list;

}
