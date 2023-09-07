package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.QuoteCurrencyEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 计价货币表 - 汇率引入时对应的计价货币
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-06-12 14:20:42
 */
@Mapper
public interface QuoteCurrencyMapper extends BaseMapper<QuoteCurrencyEntity> {

    /**
     * 获取所有的计价货币
     * @return
     */
    List<String> getAllQuoteCurrency();
	
}
