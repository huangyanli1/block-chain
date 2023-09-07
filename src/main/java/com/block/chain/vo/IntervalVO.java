package com.block.chain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-16 10:17:17
 */
@Data
public class IntervalVO {


	/**
	 * 历史行情表名
	 */
	private String tableName;
	/**
	 * K线类型 (数据点间隔)[1m,5m,15m,30m,1h,6h,1d],默认5m
	 */
	private String intervalType;

	/**
	 * 开始时间时间戳
	 */
	private String startTime;
	/**
	 * 结束时间时间戳
	 */
	private String endTime;


}
