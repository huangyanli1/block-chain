package com.block.chain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 
 * 公链交易记录返回参数 - VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class TransactionChainVO {
	//交易所在的区块的高度
	private Integer blockHeight;
	//时间戳
	private Long blockTime;
    //交易所在的区块的hash
	private String txHash;
	//金额：正数表示收入，负数表示支出
//	private BigDecimal diff;
	private String diff;
	//燃油费
	private String transactionFee;
	//合约地址
	private String contractAddress;
	//币类型 1: 公链主币  2: ERC 20类代币  3: OMNI 类代币
	private String coinType;
    //发送方地址:只有比特币家族的返回值里才可能是多个地址，其他的链都是一个或者没有。而且比特币家族可能from 和 to 都是同一个地址。
	private List<String> fromAddr;
    //接收方地址
	private List<String> toAddr;
	/**
	 * 1: received 服务端已经收到，但未广播
	 * 2: broadcasting 正在广播
	 * 3: unconfirmed 已经广播，尚未确认
	 * 4: confirmed 交易已经确认
	 * 5: 异常状态
	 */
	private Integer transactionStatus;

	/**
	 * 交易时币价格
	 */
	private BigDecimal coinPrice;
	/**
	 * 交易总价值(交易币数量乘以币价格)
	 */
	private BigDecimal coinValue;

}
