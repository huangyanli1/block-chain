package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 公司钱包数据统计表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-29 13:14:32
 */
@Data
@TableName("statistic_wallet")
public class StatisticWalletEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键Id
	 */
	@TableId
	private Long id;
	/**
	 * 公链类型(ETH BTC TRX MATIC HECO BSC)
	 */
	private String net;
	/**
	 * 公链下对应地址统计数量
	 */
	private Integer addressNumber;
	/**
	 * 公链下对应主币数量
	 */
	private BigDecimal primaryCurrencyNumber;
	/**
	 * 主币价格
	 */
	private BigDecimal currentPrice;
	/**
	 * 总价值(主币数量乘以价格)
	 */
	private BigDecimal totalValue;
	/**
	 * 统计类型(1:公链统计数据 2：总价值统计 公链统计所有的总价值之和)
	 */
	private Integer statisticStatus;
	/**
	 * 创建时间
	 */
	private Date createTime;

}
