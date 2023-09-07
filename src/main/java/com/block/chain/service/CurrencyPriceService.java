package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.utils.ResponseData;



/**
 * 币种价格
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-17 18:45:18
 */
public interface CurrencyPriceService extends IService<CurrencyPriceEntity> {

    /**
     * 按条件筛选价格表中币种的实时价格
     * @param symbol
     * @return
     */
    public ResponseData getCurrencyPriceList(String symbol);


    /**
     * 按symbol（字符串用逗号分隔）筛选价格表中币种的实时价格
     * @param symbols
     * @return
     */
    public ResponseData  getSymbolPriceList(String symbols);


    /**
     * 根据传入主币和代币分别获取主币代币价格，以及主币和代币的汇率
     * @param net
     * @param symbol
     * @return
     */
    public ResponseData  getNetSymbolRate(String net,String symbol);
    public ResponseData getNetSymbolRate(String contractAddress);

}

