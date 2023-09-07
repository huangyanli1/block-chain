package com.block.chain.controller;


import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.WalletCurrencyTransactionEntity;
import com.block.chain.service.*;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.convert.ExchangeRateParamerVO;
import com.block.chain.vo.convert.ExchangeTransactionParamerVO;
import com.block.chain.vo.management.TransactionParamerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Exchange NOW configuration controller
 * 兑换接入跨链桥exchange now相关功能
 * @author michael
 * @date 2022/02/10
 */
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
@RestController
@RequestMapping("/exchange")
public class ExchaneNowController {

    @Autowired
    private AvailablePairsService availablePairsService;

    @Autowired
    private ExchangeTransactionService exchangeTransactionService;


    /**
     *  Exchange Now 可用币对录入接口
     * @return
     */
    @GetMapping("getAvailablePairs")
    public ResponseData getAvailablePairs(){
        return availablePairsService.saveAvailablePairs();
    }



    /**
     * 币种对应的exchange now可用币对插入redis缓存
     * @return
     */
    @GetMapping("getSymbolAvailablePairs")
    public ResponseData getSymbolAvailablePairs(){
        return availablePairsService.getSymbolAvailablePairs();
    }


    /**
     * 通过公链net和symbol获取对应可兑换币对
     * @param net
     * @param symbol
     * @return
     */
    @CrossOrigin
    @GetMapping("getRedisAvailablePairs")
    public ResponseData getRedisAvailablePairs(@RequestParam String net ,@RequestParam String symbol){
        return availablePairsService.getRedisAvailablePairs(net,symbol);
    }


    /**
     * 拉取exchange- now可用币种并入库
     * @return
     */
    @GetMapping("pullRedisAvailableCurrencies")
    public ResponseData pullRedisAvailablePairs(){
        return availablePairsService.saveAvailableCurrencies();
    }


    /**
     * 获取exchange now 跨链桥可用可兑换币种
     * @return
     */
    @CrossOrigin
    @GetMapping("getAvailableCurrencies")
    public ResponseData getAvailableCurrencies(){
        return availablePairsService.getAvailableCurrencies();
    }

    /**
     * 获取exchange now跨链桥兑换币种 发出币可用最低兑换到的得到币数量
     * @param fromCurrency 发送币币种
     * @param fromNetwork  发送币所在公链
     * @param toCurrency   得到币币种
     * @param toNetwork    得到币所在公链
     * @return
     */
    @CrossOrigin
    @GetMapping("getMinAmount")
    public ResponseData  getMinAmount(@RequestParam String fromCurrency ,@RequestParam String fromNetwork,@RequestParam String toCurrency,@RequestParam String toNetwork){
        return availablePairsService.getMinAmount(fromCurrency,fromNetwork,toCurrency,toNetwork);
    }


    /**
     * 获取exchange now跨链桥兑换币种 发出币最低兑换到的得到币数量  - 正反方向
     * @param fromCurrency 发送币币种
     * @param fromNetwork  发送币所在公链
     * @param toCurrency   得到币币种
     * @param toNetwork    得到币所在公链
     * @return
     */
    @CrossOrigin
    @GetMapping("getPositiveNegativeAmount")
    public ResponseData  getPositiveNegativeAmount(@RequestParam String fromCurrency ,@RequestParam String fromNetwork,@RequestParam String toCurrency,@RequestParam String toNetwork){
        return availablePairsService.getPositiveNegativeAmount(fromCurrency,fromNetwork,toCurrency,toNetwork);
    }


    /**
     * 获取exchange now跨链桥兑换币种 发出币预计兑换到的得到币数量
     * @param fromCurrency 发送币币种
     * @param fromNetwork  发送币所在公链
     * @param toCurrency   得到币币种
     * @param toNetwork    得到币所在公链
     * @return
     */
    @CrossOrigin
    @GetMapping("getEtimatedAmount")
    public ResponseData  getEtimatedAmount(@RequestParam String fromCurrency ,@RequestParam String fromNetwork,@RequestParam String toCurrency,@RequestParam String toNetwork,@RequestParam BigDecimal fromAmount){
        return availablePairsService.getEtimatedAmount(fromCurrency,fromNetwork,toCurrency,toNetwork,fromAmount);
    }


    /**
     * 跨链桥 - 调用exchange now跨链桥创建兑换交易
     * @param vo 创建兑换交易参数
     * @return
     */
    @PostMapping("/createExchangeTransaction")
    public ResponseData  createExchangeTransaction(@RequestBody ExchangeTransactionParamerVO vo){
        return availablePairsService.createExchangeTransaction(vo);
    }



    /**
     * 根据跨链桥订单Id获取跨链桥订单交易状态
     * 订单创建超过两小时还未处理的视为交易订单失败
     */
    @GetMapping("updateTransactionStatus")
    public ResponseData updateTransactionStatus(){
        return availablePairsService.updateTransactionStatus();
    }

    @GetMapping("decryptTransactionInfo")
    public ResponseData decryptTransactionInfo(@RequestParam String encodedString){
        return exchangeTransactionService.decryptTransactionInfo(encodedString);
    }



    /**
     * 兑换列表查询接口  - exchange now
     * @param vo 查询参数
     * @return
     */
    @PostMapping("/historyExchangeTransactionList")
    public ResponseData getHistoryExchangeTransactionList(@RequestBody TransactionParamerVO vo){
        return exchangeTransactionService.getHistoryExchangeTransactionList(vo);
    }

    /**
     * 根据订单Id获取兑换详情
     * @param orderId
     * @return
     */
    @GetMapping("getExchangeTransactionByOrderId")
    public ResponseData getExchangeTransactionByOrderId(@RequestParam String orderId){
        return exchangeTransactionService.getExchangeTransactionByOrderId(orderId);
    }


    /**
     * 根据订单Id修改兑换订单状态
     * @param orderId
     * @return
     */
    @GetMapping("updateTransactionByOrderId")
    public ResponseData updateTransactionByOrderId(@RequestParam String orderId){
        return exchangeTransactionService.updateTransactionByOrderId(orderId);
    }
}
