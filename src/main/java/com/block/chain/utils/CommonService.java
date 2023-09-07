package com.block.chain.utils;

import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.mapper.CurrencyPriceMapper;
import com.block.chain.mapper.QuotationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 常用的公共方法
 */
@Service
public class CommonService {

    @Autowired
    private CurrencyPriceMapper currencyPriceMapper;

    @Autowired
    private QuotationMapper quotationMapper;

    /**
     * 获取所有币种对应价格
     * @return
     */
    public Map<String, BigDecimal> getPriceMap(){
        Map<String,BigDecimal> priceMap = new HashMap<>();
        List<CurrencyPriceEntity> list = currencyPriceMapper.getCurrencyPriceList(null);
        for(CurrencyPriceEntity entity : list){
            String currencySymbol = entity.getCurrencySymbol();
            BigDecimal priceUsd = entity.getPriceUsd();
            priceMap.put(currencySymbol,priceUsd);
        }
        return priceMap;
    }


    /**
     * 计算偏差 ： （旧的汇率 减去 新的汇率）除以 旧的汇率 = 偏差滑点
     * 判断偏差滑点是否大于1  isPoint 为true 偏差滑点在百分之一以内 为false 偏差滑点在百分之一以外
     * @param oldExchangeRate 旧的汇率 - 估算汇率 - 交易按钮点击时上一次的汇率
     * @param exchangeRate 现有汇率 - 精准汇率 - 交易按钮点击时计算的汇率
     * @return
     */
    public Boolean isSlipPoint(BigDecimal oldExchangeRate ,BigDecimal exchangeRate){
        Boolean isPoint = false;
        //计算偏差 ： （旧的汇率 减去 新的汇率）除以 旧的汇率 = 偏差滑点
        BigDecimal difference  =(oldExchangeRate.subtract(exchangeRate)).divide(oldExchangeRate,BigDecimal.ROUND_FLOOR);
        BigDecimal min = new BigDecimal(-0.01);
        BigDecimal max = new BigDecimal(0.01);
        //判断偏差滑点是否大于1  slippage 为1 偏差滑点在百分之一以内 为0 偏差滑点在百分之一以外
        if(difference.compareTo(min)>=0 && difference.compareTo(max) <= 0){
            isPoint = true;
        }else{
            isPoint = false;
        }
        return isPoint;
    }


    /**
     * 计算币种汇率
     * @param entrySymbol 入账币种
     * @param outSymbol 出账币种
     * @return
     */
    public BigDecimal getExchangeRate(String entrySymbol,String outSymbol){
        Map<String, BigDecimal> price = getPriceMap();
        BigDecimal entryPrice = price.get(entrySymbol);
        BigDecimal outPrice = price.get(outSymbol);
        //入账币种和出账币种汇率 ， 入账币种价格除以出账币种价格
        BigDecimal exchangeRate = entryPrice.divide(outPrice,BigDecimal.ROUND_FLOOR);
        return exchangeRate;
    }


    /**
     * 判断时间戳是否为毫秒
     * @param timestamp 时间戳
     * @return
     */
    public Boolean isMilliseSecond(String timestamp){
        if(timestamp.length() == 10){
            return false;
        }else if(timestamp.length() == 13){
            return true;
        }else{
            return false;
        }
    }


    /**
     * 校验方法 - 校验表名是否存在数据库中
     * @param tableName
     * @return
     */
    public  Boolean isHaveTable(String tableName){
        Boolean isHaveTable = false;
        int size = quotationMapper.isHaveTable(tableName);
        if( size > 0 ){
            isHaveTable = true;
        }else {
            isHaveTable = false;
        }
        return isHaveTable;
    }


}
