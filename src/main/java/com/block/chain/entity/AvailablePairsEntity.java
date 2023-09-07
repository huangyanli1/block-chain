package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Exchage Now 跨链桥支持的可用币对
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-10 11:16:06
 */
@Data
@TableName("available_pairs")
public class AvailablePairsEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键Id
	 */
	@TableId
	private Long id;
	/**
	 * 发出币symbol - 兑换扣除
	 */
	private String fromCurrency;
	/**
	 * 发出币所在链 - 兑换扣除
	 */
	private String fromNetwork;
	/**
	 * 得到币symbol - 兑换得到
	 */
	private String toCurrency;
	/**
	 * 得到币所在链 - 兑换得到
	 */
	private String toNetwork;
	/**
	 * 交换流类型- 固定费率，标准
	 */
	private String flow;

}
