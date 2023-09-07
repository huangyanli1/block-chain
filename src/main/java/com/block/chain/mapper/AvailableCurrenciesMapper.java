package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.AvailableCurrenciesEntity;
import com.block.chain.entity.AvailablePairsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Exchage Now 跨链桥支持的可用币种
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-11 13:42:32
 */
@Mapper
public interface AvailableCurrenciesMapper extends BaseMapper<AvailableCurrenciesEntity> {


    /**
     * 清空所有从exchange-now获取的可用币种
     * @return
     */
    public int remove();


    /**
     * 批量插入exchange now可用币对
     * @param data
     * @return
     */
    int  insertAvailableCurrencies(@Param("data") AvailableCurrenciesEntity data);
}
