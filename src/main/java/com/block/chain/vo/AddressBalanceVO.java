package com.block.chain.vo;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 
 * 拿取地址下余额对应VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class AddressBalanceVO {
	/**
	 * 主币余额
	 */
	private String balance;

	private List<BalanceTokensVO> tokens;


}
