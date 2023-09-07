package com.block.chain.controller;


import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.WalletCurrencyTransactionEntity;
import com.block.chain.entity.WalletTransactionEntity;
import com.block.chain.service.*;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.AddressParameterVO;
import com.block.chain.vo.TransactionInfoVO;
import com.block.chain.vo.TransactionParamsVO;
import com.block.chain.vo.convert.ExchangeRateParamerVO;
import com.block.chain.vo.management.TransactionParamerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Currency exchanget configuration controller
 * 币币兑换相关功能
 * @author michael
 * @date 2022/12/07
 */
@RestController
@RequestMapping("/convert")
public class CurrencyConvertController {

    @Autowired
    private CurrencyConvertService currencyConvertService;

    @Autowired
    private WalletCurrencyTransactionService walletCurrencyTransactionService;

    @Autowired
    private CurrencyTypeService currencyTypeService;

    /**
     * 币币兑换订单录入接口 - 对接去中心化交易所
     * @param entity 订单交易信息
     * @return
     */
    @CrossOrigin
    @PostMapping("/saveCurrencyTransactionInfo")
    public ResponseData saveCurrencyTransactionInfo(@RequestBody WalletCurrencyTransactionEntity entity){
        return walletCurrencyTransactionService.saveCurrencyTransactionInfo(entity);
    }


    /**
     * 兑换列表查询接口
     * @param vo 查询参数
     * @return
     */
    @PostMapping("/currencyTransactionList")
    public ResponseData getCurrencyTransactionList(@RequestBody TransactionParamerVO vo){
        return walletCurrencyTransactionService.getCurrencyTransactionList(vo);
    }

    /**
     * 币币兑换交易信息 - 详情查询接口
     * @param id 币币兑换订单ID
     * @return
     */
    @GetMapping("getCurrencyTransactionById")
    public ResponseData getCurrencyTransactionById(@RequestParam String id){
        return walletCurrencyTransactionService.getCurrencyTransactionById(id);
    }

    /**
     * 获取所有状态为broadcasting币币兑换交易数据
     * @return
     */
    @GetMapping("getBroadcastingList")
    public ResponseData getBroadcastingList(){
        return walletCurrencyTransactionService.getBroadcastingList();
    }

    /**
     * 币币兑换订单信息 - 订单状态修改
     * @param list
     * @return
     */
    @PostMapping("/updateCurrencyTransaction")
    public ResponseData updateCurrencyTransaction(@RequestBody List<WalletCurrencyTransactionEntity> list) {
        return walletCurrencyTransactionService.updateCurrencyTransaction(list);
    }


    /**
     * 获取所有symbol数据 - 对symbol去重
     * @return
     */
    @GetMapping("getSymbolList")
    public ResponseData  getSymbolList(){
        return currencyTypeService.getSymbolList();
    }



    /**
     * 获取公链为ETH和MATIC下可兑换币种
     * @return
     */
    @CrossOrigin
    @GetMapping("getExchangeList")
    public ResponseData  getExchangeList(){
        return currencyTypeService.getExchangeList();
    }

    /**
     * 转发前端接口，调用链上业务信息接口
     * @param json
     * @return
     */
    @CrossOrigin
    @PostMapping("transferOnChain")
    public Object transferOnChain(@RequestBody JSONObject json){
        return currencyConvertService.transferOnChain(json);
    }















    /**
     * 币币兑换 - 用户输入入账币种数量 - 获取入账出账币种汇率，以及出账币数量
     * 入账币种兑换出账币种数量
     * @return
     */
    @PostMapping("/estimatedExchangeRate")
    public ResponseData getEstimatedExchangeRate(@RequestBody ExchangeRateParamerVO vo){
        return currencyConvertService.getEstimatedExchangeRate(vo);
    }


    /**
     * 币币兑换 - 用户点击交易按钮时 - 获取点击时刻入账出账币种汇率并和上一次汇率比较偏差是否大于一个滑点（即百分之一）
     * 入账币种兑换出账币种数量
     * @return
     */
    @PostMapping("/preciseExchangeRate")
    public ResponseData getPreciseExchangeRate(@RequestBody ExchangeRateParamerVO vo){
        return currencyConvertService.getPreciseExchangeRate(vo);
    }
}
