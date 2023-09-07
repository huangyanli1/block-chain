package com.block.chain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * symbol-slug对应信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-12-09 14:16:09
 */
@Data
@TableName("symbol_slug_info")
public class SymbolSlugInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键Id
	 */
	@TableId
	private Long id;
	/**
	 * 币种符号
	 */
	private String symbol;
	/**
	 * 币种名称（ID）
	 */
	private String slug;

}
