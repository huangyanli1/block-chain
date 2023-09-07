package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CompanyAddressEntity;
import com.block.chain.entity.CurrencyChainInfoEntity;
import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.entity.CurrencyTypeEntity;
import com.block.chain.entity.mongo.CompanyReceiveVO;
import com.block.chain.entity.mongo.CurrencyReceiveVO;
import com.block.chain.entity.mongo.SymbolCurrencyVO;
import com.block.chain.entity.mongo.TRX;
import com.block.chain.mapper.*;
import com.block.chain.service.CurrencyConvertService;
import com.block.chain.service.MongoDBService;
import com.block.chain.utils.*;
import com.block.chain.vo.TransactionAddressVO;
import com.block.chain.vo.TransactionBtcInfoVO;
import com.block.chain.vo.bussiness.OrderNotificationVO;
import com.block.chain.vo.convert.ExchangeRateParamerVO;
import com.block.chain.vo.management.MemberAssetVO;
import com.block.chain.vo.management.MemberSymbolVO;
import com.block.chain.vo.management.ReceiveStatisticVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

/**
 * CurrencyConvert  related service -
 * 币币兑换相关业务功能实现类
 */
@Service("currencyConvertService")
@Slf4j
public class CurrencyConvertServiceImpl implements CurrencyConvertService {

    @Autowired
    private CommonService commonService;

    @Value("${EXTERNA.TransferUrl}")
    private String transferUrl;

    @Autowired
    private RestTemplate restTemplate;



    public ResponseData getEstimatedExchangeRate(ExchangeRateParamerVO vo){
        JSONObject json = new JSONObject();
        String entrySymbol = vo.getEntrySymbol();
        String outSymbol = vo.getOutSymbol();
        BigDecimal entryAmount = vo.getEntryAmount();
        if(StringUtils.isEmpty(entrySymbol)||StringUtils.isEmpty(outSymbol)){
            return ResponseData.fail("入账出账币种缺失！");
        }
        if(entryAmount.compareTo(BigDecimal.ZERO)==0){
            return ResponseData.fail("入账币种数量为0!");
        }
        Map<String, BigDecimal> price = commonService.getPriceMap();

        BigDecimal entryPrice = price.get(entrySymbol);
        BigDecimal outPrice = price.get(outSymbol);
        if(entryPrice == null || outPrice == null){
            return ResponseData.fail("入账出账币种价格缺失!");
        }
        BigDecimal entryValue = entryAmount.multiply(entryPrice);
        BigDecimal exchangeRate = entryPrice.divide(outPrice,BigDecimal.ROUND_FLOOR);
        BigDecimal outAmount = entryValue.divide(outPrice,BigDecimal.ROUND_FLOOR);


        json.put("exchangeRate",exchangeRate);
        json.put("outAmount",outAmount);
        return ResponseData.ok(json);
    }


    public ResponseData getPreciseExchangeRate(ExchangeRateParamerVO vo){
        JSONObject json = new JSONObject();
        String entrySymbol = vo.getEntrySymbol();
        String outSymbol = vo.getOutSymbol();
        BigDecimal entryAmount = vo.getEntryAmount();
        BigDecimal oldExchangeRate = vo.getOldExchangeRate();
        if(StringUtils.isEmpty(entrySymbol)||StringUtils.isEmpty(outSymbol)){
            return ResponseData.fail("入账出账币种缺失！");
        }
        if(entryAmount.compareTo(BigDecimal.ZERO)==0){
            return ResponseData.fail("入账币种数量为0!");
        }
        if(oldExchangeRate == null){
            return ResponseData.fail("上一次币种汇率数据为空!");
        }
        Map<String, BigDecimal> price = commonService.getPriceMap();

        BigDecimal entryPrice = price.get(entrySymbol);
        BigDecimal outPrice = price.get(outSymbol);
        if(entryPrice == null || outPrice == null){
            return ResponseData.fail("入账出账币种价格缺失!");
        }
        BigDecimal entryValue = entryAmount.multiply(entryPrice);
        BigDecimal exchangeRate = entryPrice.divide(outPrice,BigDecimal.ROUND_FLOOR);
        BigDecimal outAmount = entryValue.divide(outPrice,BigDecimal.ROUND_FLOOR);

        Boolean isPoint = commonService.isSlipPoint(oldExchangeRate ,exchangeRate);
        if(isPoint){
            Integer slippage = 1;
            json.put("slippage",slippage);
        }else{
            Integer slippage = 0;
            json.put("slippage",slippage);
        }
        json.put("exchangeRate",exchangeRate);
        json.put("outAmount",outAmount);
        return ResponseData.ok(json);
    }


    public Object transferOnChain(JSONObject json){
        if(json == null){
            return ResponseData.fail("参数体为空！");
        }
        Object result = new Object();
        try {
            HttpEntity<JSONObject> request = new HttpEntity<>(json,null);
            result = restTemplate.postForObject(transferUrl,request,Object.class);
            String s = "";
        }catch (Exception e){
            e.printStackTrace();
            log.error("转发链上业务推送执行失败="+ e+"返回值信息=="+result.toString());
            return null;
        }
        return result;

    }



}