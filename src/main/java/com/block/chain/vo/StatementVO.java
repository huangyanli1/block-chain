package com.block.chain.vo;

import com.block.chain.entity.CurrencyTypeEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 
 * 报表VO
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Data
public class StatementVO {

	/**
	 * 币种
	 */
	List<CurrencyTypeEntity> currencyList;

}
