package com.block.chain.vo.management;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 后台管理系统 - 加密货币交易管理 - 接收 ， 发送币列表查询参数VO
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-28 17:10:16
 */
@Data
public class TransactionParamerVO {
	/**
	 * 地址列表
	 */
	private List<String> address;
	/**
	 * 交易hash
	 */
	private String txHash;
	/**
	 * 是否到账 0 否 1 是
	 */
	private Integer isConfim;
	/**
	 * 开始时间（时间戳 - 秒）
	 */
	private Long startTime;
	/**
	 * 结束时间（时间戳 - 秒）
	 */
	private Long endTime;
	/**
	 * 当前页
	 */
	private Integer page;
	/**
	 * 每页展示数量
	 */
	private Integer pageSize;
	/**
	 * 币种符号
	 */
	private  String symbol;

	/**
	 * 最小发送数量
	 */
	private Integer minDiff;

	/**
	 * 最大发送数量
	 */
	private Integer maxDiff;

	/**
	 * 订单Id(去中心化交易所订单Id)
	 */
	private String orderId;

	/**
	 * 入账币 - 币种符号
	 */
	private String entrySymbol;

	/**
	 * 出账币-币种符号
	 */
	private String outSymbol;

	/**
	 * 地址列表
	 */
	private List<String> orderList;

	/**
	 * id集合
	 */
	private List<Long> idList;


}
