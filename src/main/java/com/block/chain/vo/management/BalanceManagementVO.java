package com.block.chain.vo.management;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BalanceManagementVO {

	/**
	 * 公链
	 */
	private  String net;

	/**
	 * 币种
	 */
	private List<String> address;

}
