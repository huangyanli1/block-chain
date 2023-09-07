package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.entity.CurrencyTypeEntity;
import com.block.chain.mapper.CurrencyPriceMapper;
import com.block.chain.mapper.CurrencyTypeMapper;
import com.block.chain.service.CurrencyPriceService;
import com.block.chain.utils.ResponseData;
import com.block.chain.utils.ToolUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


@Service("currencyPriceService")
public class CurrencyPriceServiceImpl extends ServiceImpl<CurrencyPriceMapper, CurrencyPriceEntity> implements CurrencyPriceService {


    @Autowired
    private CurrencyPriceMapper currencyPriceMapper;

    @Autowired
    private CurrencyTypeMapper currencyTypeMapper;



    /**
     * 按条件筛选价格表中币种的实时价格
     * @param symbol
     * @return
     */
    public ResponseData getCurrencyPriceList(String symbol){
        List<CurrencyPriceEntity> list = new ArrayList<>();
        list = currencyPriceMapper.getCurrencyPriceList(symbol);
       return  ResponseData.ok(list);
    }


    /**
     * 按symbol（字符串用逗号分隔）筛选价格表中币种的实时价格
     * @param symbols
     * @return
     */
    public ResponseData  getSymbolPriceList(String symbols){
        if(StringUtils.isEmpty(symbols)){
            return ResponseData.fail("查询参数有误！");
        }
        List<String> symbolList = new ArrayList<>();
        String[] symbolArray = ToolUtils.str2StrArray(symbols,",");
        for(String symbol : symbolArray){
            symbolList.add(symbol);
        }
        List<CurrencyPriceEntity> priceList = currencyPriceMapper.getSymbolPriceList(symbolList);
        return ResponseData.ok(priceList);
    }


    /**
     * 根据传入主币和代币分别获取主币代币价格，以及主币和代币的汇率
     * @param net
     * @param symbol
     * @return
     */
    public ResponseData  getNetSymbolRate(String net,String symbol){
        if(StringUtils.isEmpty(net)||StringUtils.isEmpty(symbol)){
            return ResponseData.fail("查询参数有误！");
        }
        JSONObject obj = new JSONObject();

        List<CurrencyPriceEntity> netList = currencyPriceMapper.getCurrencyPriceList(net);
        List<CurrencyPriceEntity> symbolList = currencyPriceMapper.getCurrencyPriceList(symbol);
        BigDecimal netPrice = netList != null ? netList.get(0).getPriceUsd():new BigDecimal("0");
        BigDecimal symbolPrice = symbolList != null ? symbolList.get(0).getPriceUsd():new BigDecimal("0");
        BigDecimal rate = netPrice.multiply(symbolPrice);
        obj.put("netPriceUsd",netPrice);
        obj.put("symbolPriceUsd",symbolPrice);
        obj.put("rate",rate);
        return ResponseData.ok(obj);
    }

    public ResponseData getNetSymbolRate(String contractAddress){
        if(StringUtils.isEmpty(contractAddress)){
            return ResponseData.fail("查询参数有误");
        }
        JSONObject obj = new JSONObject();
        CurrencyTypeEntity currency = currencyTypeMapper.getCurrencyContract(contractAddress);
        String net = currency.getNet();
        String symbol = currency.getSymbol();
        CurrencyTypeEntity mainCurrency =currencyTypeMapper.getMainCurrencyInfo(net);
        String netSymbol = mainCurrency.getSymbol();
        List<CurrencyPriceEntity> netList = currencyPriceMapper.getCurrencyPriceList(netSymbol);
        List<CurrencyPriceEntity> symbolList = currencyPriceMapper.getCurrencyPriceList(symbol);
        BigDecimal netPrice = netList != null ? netList.get(0).getPriceUsd():new BigDecimal("0");
        BigDecimal symbolPrice = symbolList != null ? symbolList.get(0).getPriceUsd():new BigDecimal("0");
        BigDecimal rate = netPrice.multiply(symbolPrice);
        obj.put("netPriceUsd",netPrice.toPlainString());
        obj.put("symbolPriceUsd",symbolPrice.toPlainString());
        obj.put("rate",rate.toPlainString());
        return ResponseData.ok(obj);
    }


}