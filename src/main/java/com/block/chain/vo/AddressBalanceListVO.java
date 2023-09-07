package com.block.chain.vo;

import lombok.Data;

import java.util.List;

/**
 * 
 * 批量拿取地址下余额对应VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class AddressBalanceListVO {
	/**
	 * 主币余额
	 */
	private String balance;

	private String net;

	private String address;

	private List<BalanceTokensVO> tokens;


}
