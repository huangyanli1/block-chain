package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * Exchage Now 跨链桥支持的可用币种
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-11 13:42:32
 */
@Data
@TableName("available_currencies")
public class AvailableCurrenciesEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键Id
	 */
	@TableId
	private Long id;
	/**
	 * exchange now币种symbol
	 */
	private String ticker;
	/**
	 * exchange now对应的币种full_name
	 */
	private String name;
	/**
	 * 币种图片
	 */
	private String image;
	/**
	 * exchange now对应的币种公链
	 */
	private String network;
	/**
	 * 录入时间
	 */
	private Date createDate;

}
