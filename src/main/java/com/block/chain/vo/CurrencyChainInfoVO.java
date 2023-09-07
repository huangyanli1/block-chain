package com.block.chain.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 公链信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-18 14:12:58
 */
@Data
public class CurrencyChainInfoVO implements Serializable {
	private static final long serialVersionUID = 1L;

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
