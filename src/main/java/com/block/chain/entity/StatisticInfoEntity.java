package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 统计信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-25 18:03:45
 */
@Data
@TableName("statistic_info")
public class StatisticInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键Id
	 */
	@TableId
	private Long id;
	/**
	 * 币种信息
	 */
	private String currencyInfo;
	/**
	 * 地址信息
	 */
	private String addressInfo;
	/**
	 * 币种总价值
	 */
	private String currencyValue;
	/**
	 * 交易异常信息
	 */
	private String transactionException;
	/**
	 * 今日入账笔数
	 */
	private String entriesNumber;
	/**
	 * 今日入账数量
	 */
	private String entriesInfo;
	/**
	 * 今日提现笔数
	 */
	private String withdrawalNumber;
	/**
	 * 今日提现数量
	 */
	private String withdrawalInfo;
	/**
	 * 统计日期 减一天
	 */
	private Long createTime;

}
