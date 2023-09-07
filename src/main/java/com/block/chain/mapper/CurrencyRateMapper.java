package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.CurrencyRateEntity;
import com.block.chain.vo.CurrencyRateVO;
import com.block.chain.vo.ExchangeRateVO;
import com.block.chain.vo.management.FiatRateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 法币汇率信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-06-06 11:25:00
 */
@Mapper
public interface CurrencyRateMapper extends BaseMapper<CurrencyRateEntity> {


    List<CurrencyRateEntity> getCurrencyRate(@Param("sourceCurrency") String sourceCurrency, @Param("quoteCurrency") String quoteCurrency);

    /**
     * 获取所有监控了汇率的法币列表
     * @return
     */
    List<CurrencyRateVO>  getFiatList();

    /**
     * 据通过法币获取对应美元的汇率 - 只针对美元
     * @param sourceCurrency
     * @param quoteCurrency
     * @return
     */
    ExchangeRateVO exchangeRate(@Param("sourceCurrency") String sourceCurrency, @Param("quoteCurrency") String quoteCurrency);

    /**
     * 获取所有监控了汇率的法币列表
     * @return
     */
    List<FiatRateVO> messageFiatList();

    List<FiatRateVO> getSymbolRateList(@Param("symbolList") List<String> symbolList);

    FiatRateVO getSymbolRate(@Param("sourceCurrency") String sourceCurrency);


}
