package com.block.chain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 用户下所有地址余额信息以及统计信息
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-03 17:10:16
 */
@Data
public class BalanceStatisticsVO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 所有地址对应的余额信息
	 */
	private List<RecoverBalanceVO> balanceList;

	/**
	 *用户下所有地址币种统计信息以及总价值信息
	 */
	private List<UserBalanceVO> userList;

}
