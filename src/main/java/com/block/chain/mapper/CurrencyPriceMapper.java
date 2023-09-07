package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.entity.QuotationEntity;
import com.block.chain.vo.IntervalVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 币种价格
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-17 18:45:18
 */
@Mapper
public interface CurrencyPriceMapper extends BaseMapper<CurrencyPriceEntity> {

    /**
     * 获取价格表中是否有该symbol的数据
     * @param symbol 币种符号
     * @return
     */
    List<CurrencyPriceEntity> getCurrencyPrice(@Param("symbol") String symbol);

    /**
     * 按条件筛选价格表中币种的实时价格
     * @param currencySymbol
     * @return
     */
    List<CurrencyPriceEntity> getCurrencyPriceList(@Param("currencySymbol") String currencySymbol);


    /**
     * 按symbol（字符串用逗号分隔）筛选价格表中币种的实时价格
     * @param symbolList
     * @return
     */
    List<CurrencyPriceEntity> getSymbolPriceList(@Param("symbolList") List<String> symbolList);


    /**
     * 获取所有监控了价格的数字货币列表
     * @return
     */
    List<CurrencyPriceEntity> getALLCurrencyPrice();


}
