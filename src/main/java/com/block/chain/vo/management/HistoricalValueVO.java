package com.block.chain.vo.management;

import com.block.chain.vo.TransactionAddressVO;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 后台管理系统 - 加密货币交易管理 - 接收 ， 发送币列表查询参数VO
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-11-28 17:10:16
 */
@Data
public class HistoricalValueVO {

	/**
	 * 会员或团队名下对应的地址信息
	 */
	private Map<String , List<TransactionAddressVO>> teamParamerMap;


	/**
	 * type 对应不同页面的逻辑 1 发送页面  2 接收页面 3 兑换页面
	 */
	private String type;

}
