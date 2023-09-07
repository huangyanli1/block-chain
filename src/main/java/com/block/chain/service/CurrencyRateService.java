package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.CurrencyRateEntity;
import com.block.chain.utils.ResponseData;


import java.util.Map;

/**
 * 法币汇率信息表
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-06-06 11:25:00
 */
public interface CurrencyRateService extends IService<CurrencyRateEntity> {

    /**
     * 获取所有法币，并集成法币汇率
     * @return
     */
    public ResponseData integratedRate();

    /**
     * 获取法币汇率，需要接入外部数据
     * @return
     */
    public ResponseData getFiatRate(String sourceCurrency,String quoteCurrency);

    /**
     * 获取所有监控了汇率的法币列表
     * @return
     */
    public ResponseData getFiatList();

    /**
     * 获取所有法币，并集成法币汇率 - 汇率集成的补充接口
     * @return
     */
    public ResponseData integratedSupplementaryRate();

    /**
     * 获取所有监控了价格的数字货币列表
     * @return
     */
    public ResponseData getALLCurrencyPrice();

    /**
     * 通过法币获取对应美元的汇率 - 该接口只针对美元汇率
     * @return
     */
    public ResponseData exchangeRate(String target);


    /**
     * 更新所有法币对应的美元汇率 - 通过蜜蜂查进行更新集成
     * @return
     */
    public ResponseData mifengchaExchangeRate();

    /**
     * 推送所有更新的法币汇率 - 仅仅适用于消息推送
     * @return
     */
    public ResponseData sendFiatRateMessage();

    /**
     * 通过法币获取所有对应美元的汇率 - 该接口只针对美元汇率
     * @return
     */
    public ResponseData getSymbolsRateList(String symbols);

    /**
     * 通过法币获取对应美元的汇率 - 该接口只针对美元汇率
     * @return
     */
    public ResponseData getSymbolsRate(String symbol);


}

