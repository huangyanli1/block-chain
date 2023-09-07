package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 钱包交易
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-17 14:51:23
 */
@Data
@TableName("wallet_transaction")
public class WalletTransactionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId
	private Long id;
	/**
	 * 交易hash 
	 */
	private String txHash;
	/**
	 * 公链，目前值只有6个: 
BTC, ETH, MATIC, BSC, HECO, TRX
	 */
	private String net;
	/**
	 * 交易发起的时间
13位毫秒时间戳
	 */
	private Long txCommitTime;
	/**
	 * 业务线id.
0: 用户在在Ulla生态外部发起的交易，即: 不是Ulla合作方或Ulla自有系统发起的交易
1: Ulla 钱包
	 */
	private Integer businessId;
	/**
	 * Business order id. 业务线的自定义ID。 资管只保存，透传，不做修改。同时提供查询接口。
	 */
	private String businessOrderId;
	/**
	 * 钱包系统唯一交易ID。 该ID和法币交易ID也不同。 可以用该ID查出对应的交易记录。
	 */
	private String orderId;
	/**
	 * 交易所在的区块的hash. 当status是 1, 2的时候，值为空，其他时候必须有值
	 */
	private String blockHash;
	/**
	 * 交易所在的区块的高度. 当status是 1, 2的时候，值为空，其他时候必须有值
	 */
	private Integer blockNumber;
	/**
	 * 服务端收到交易请求的时间
	 */
	private Long receivedTime;
	/**
	 * 服务端开始广播的时间
	 */
	private Long broadcastingTime;
	/**
	 * 广播完毕，交易未确认的时间
	 */
	private Long unconfirmedTime;
	/**
	 * 交易确认时间
	 */
	private Long confirmedTime;
	/**
	 * 交易所处的状态。目前广播是客户端完成，所以服务端只有3,4 两种状态
1: received 服务端已经收到，但未广播
2: broadcasting 正在广播
3: unconfirmed 已经广播，尚未确认
4: confirmed 交易已经确认
跟客户端返回数据的时候，需要问清楚客户端要整数值，还是字符串文字意义
	 */
	private Integer transactionStatus;
	/**
	 * 交易币类型
1：公链主币:2：ERC 20代币:3：OMNI 31代币:4：ERC 721代币:5：ERC 1155代币
	 */
	private Integer coinType;
	/**
	 * 发方地址
	 */
	private String fromAddr;
	/**
	 * 收方地址
	 */
	private String toAddr;
	/**
	 * 合约地址. 仅在coinType值为 2, 4, 5时必填，其他时候可不填
	 */
	private String contractAddr;
	/**
	 * 交易金额。小数点后保留18位。 单位为常用主币单位。 如:
BTC: 单位为BTC, 不用聪
TRX: 单位为TRX, 不用孙
USDT: 单位为USTD，不用 0.000001
	 */
	private BigDecimal transactionValue;
	/**
	 * transactionValue的小数点后的精度
	 */
	private Integer decimalNumber;
	/**
	 * 设定的交易手续费上限. 单位为公链主币最小数据单位. 如:
BTC: 单位为 聪
ETH: 单位为 Wei
	 */
	private BigDecimal feeLimit;
	/**
	 * 实际使用的交易手续费上限. 单位为公链主币最小数据单位. 如:
BTC: 单位为 聪
ETH: 单位为 Wei
	 */
	private BigDecimal feeUsed;
	/**
	 * 含签名的交易数据
	 */
	private String txRawData;
	/**
	 * 合作方自定义透传数据. 资管不做修改，在查询交易详情时带回给传入方
	 */
	private String businessData1;
	/**
	 * 合作方自定义透传数据. 资管不做修改，在查询交易详情时带回给传入方
	 */
	private String businessData2;

	/**
	 * 币种符号
	 */
	private String symbol;
	/**
	 * 交易时币价格
	 */
	private BigDecimal coinPrice;

	/**
	 * 交易总价值(交易币数量乘以币价格)
	 */
	private BigDecimal coinValue;

}
