package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 公链信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-18 14:12:58
 */
@Data
@TableName("currency_chain_info")
public class CurrencyChainInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键Id
	 */
	@TableId
	private Long id;
	/**
	 * 主链类型
	 */
	private String net;
	/**
	 * 主链名称
	 */
	private String netName;
	/**
	 * 图标链接
	 */
	private String logoUrl;

}
