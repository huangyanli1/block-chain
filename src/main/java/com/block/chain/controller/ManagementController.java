package com.block.chain.controller;


import com.block.chain.entity.CompanyAddressEntity;
import com.block.chain.service.*;
import com.block.chain.utils.PageResult;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.CompanyAddressVO;
import com.block.chain.vo.TransactionAddressVO;
import com.block.chain.vo.management.BalanceManagementVO;
import com.block.chain.vo.management.HistoricalValueVO;
import com.block.chain.vo.management.TransactionParamerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ManagementController configuration controller
 * 后台管理系统相关
 * @author michael
 * @date 2022/11/25
 */

@RestController
@RequestMapping("/management")
public class ManagementController {

    @Autowired
    private CompanyAddressService companyAddressService;

    @Autowired
    private MongoDBService mongoDBService;

    @Autowired
    private TransactionBtcInfoService transactionBtcInfoService;

    @Autowired
    private CurrencyTypeService currencyTypeService;

    @Autowired
    private ExchangeTransactionService exchangeTransactionService;


    /**
     * 公司地址录入
     * @param list 地址信息
     * @return
     */
    @CrossOrigin
    @PostMapping("saveCompanyAddress")
    public ResponseData saveCompanyAddress(@RequestBody List<CompanyAddressEntity> list){
        return companyAddressService.saveCompanyAddress(list);
    }


    /**
     * 判断地址是否为公司地址
     * @param address 地址
     * @param net 公链
     * @return
     */
    @GetMapping("isCompanyAddress")
    public ResponseData isCompanyAddress(@RequestParam String address ,@RequestParam String net){
        return companyAddressService.isCompanyAddress(address,net);
    }


    /**
     * 财务管理 - 公司钱包地址列表信息查询
     * @param list 需要查询的地址信息
     * @return
     */
    @PostMapping("getCompanyAddressList")
    public ResponseData getCompanyAddressList(@RequestBody List<CompanyAddressVO> list){
        return companyAddressService.getCompanyAddressList(list);
    }

    /**
     * 财务管理 - 公司钱包管理列表信息查询
     * @param list 需要查询的地址信息
     * @return
     */
    @PostMapping("getCompanyManagementList")
    public ResponseData getCompanyManagementList(@RequestBody List<CompanyAddressVO> list){
        return companyAddressService.getCompanyAddressList(list);
    }


    /**
     * 后台财务管理 - 公司钱包管理入账出账统计
     * @return
     */
    @PostMapping("getQuantityStatistics")
    public ResponseData getQuantityStatistics(@RequestBody List<CompanyAddressVO> list){
        return companyAddressService.getQuantityStatistics(list);
    }



    /**
     * 后台管理系统-首页统计-接收统计
     * @return
     */
    @GetMapping("currencyReceiveStatistics")
    public ResponseData currencyReceiveStatistics(){
        return mongoDBService.currencyReceiveStatistics();
    }

    /**
     * 后台管理系统-首页统计-接收统计数据查询 -当天数据
     * @return
     */
    @GetMapping("getReceiveStatisticsList")
    public ResponseData getReceiveStatisticsList(){
        return mongoDBService.getReceiveStatisticsList();
    }


    /**
     * 后台管理系统-首页统计-发送统计
     * @return
     */
    @GetMapping("currencySendingStatistics")
    public ResponseData currencySendingStatistics(){
        return mongoDBService.currencySendingStatistics();
    }

    /**
     * 后台管理系统-首页统计-发送统计数据查询 -当天数据
     * @return
     */
    @GetMapping("getSendingStatisticsList")
    public ResponseData getSendingStatisticsList(){
        return mongoDBService.getSendingStatisticsList();
    }

    /**
     * 后台管理系统-加密货币交易管理 - 接收币列表
     * @return
     */
//    @GetMapping("getReceiveTransactionList")
//    public ResponseData getReceiveTransactionList(@RequestParam String address,@RequestParam String txHash,@RequestParam Integer isConfim,@RequestParam Long startTime,@RequestParam Long endTime,@RequestParam Integer page,@RequestParam Integer pageSize){
//        return transactionBtcInfoService.getReceiveTransactionList(address,txHash,isConfim,startTime,endTime,page,pageSize);
//    }
    @PostMapping("getReceiveTransactionList")
    public ResponseData getReceiveTransactionList(@RequestBody TransactionParamerVO vo){
        return transactionBtcInfoService.getReceiveTransactionList(vo.getAddress(),vo.getTxHash(),vo.getIsConfim(),vo.getStartTime(),vo.getEndTime(),vo.getPage(),vo.getPageSize());
    }


    /**
     * 后台管理系统-加密货币交易管理 - 发送币列表
     * @return
     */
//    @GetMapping("getSendingTransactionList")
//    public ResponseData getSendingTransactionList(@RequestParam String address,@RequestParam String symbol,@RequestParam Integer minDiff,@RequestParam Integer maxDiff,@RequestParam Long startTime,@RequestParam Long endTime,@RequestParam Integer page,@RequestParam Integer pageSize){
//        return transactionBtcInfoService.getSendingTransactionList(address,symbol,minDiff,maxDiff,startTime,endTime,page,pageSize);
//    }
    @PostMapping("getSendingTransactionList")
    public ResponseData getSendingTransactionList(@RequestBody TransactionParamerVO vo){
        return transactionBtcInfoService.getSendingTransactionList(vo.getAddress(),vo.getSymbol(),vo.getMinDiff(),vo.getMaxDiff(),vo.getStartTime(),vo.getEndTime(),vo.getPage(),vo.getPageSize());
    }

    /**
     * 后台管理系统-首页统计接收统计-财务收款总额，笔数数据查询 - 时间段（周，月）
     * @return
     */
    @GetMapping("getCompanyReceiveStatistics")
    public ResponseData getCompanyReceiveStatistics(@RequestParam Integer type){
        return mongoDBService.getCompanyReceiveStatistics(type);
    }

    /**
     * 后台管理系统-首页统计接收统计-钱包接收币笔数趋势图(币种和net查询)
     * @return
     */
    @GetMapping("getReceiveStatistics")
    public ResponseData getReceiveStatistics(@RequestParam String net,@RequestParam String symbol){
        return mongoDBService.getReceiveStatistics(net,symbol);
    }

    /**
     * 后台管理系统-首页统计发送统计-钱包发送币笔数趋势图(币种和net查询)
     * @return
     */
    @GetMapping("getSendingStatistics")
    public ResponseData getSendingStatistics(@RequestParam String net,@RequestParam String symbol){
        return mongoDBService.getSendingStatistics(net,symbol);
    }

    /**
     * 会员管理 - 会员基础信息 - 加密数字资产管理
     * @return
     */
    @PostMapping("memberAssets")
    public ResponseData getMemberAssets(@RequestBody List<TransactionAddressVO> addressList){
        return mongoDBService.getMemberAssets(addressList);
    }

    /**
     * 后台管理系统-首页统计-财务收款总额，笔数统计 - 主要统计公司地址相关数据
     * @return
     */
    @GetMapping("companyReceiveStatistics")
    public ResponseData companyReceiveStatistics(){
        return mongoDBService.currencyCompanyReceiveStatistics();
    }


    /**
     * 后台管理系统 - 币种管理列表 - 币种列表查询
     * @param page 当前页
     * @param pageSize 每页展示数量
     * @return
     */
    @GetMapping("getCurrencyPriceList")
    public ResponseData<PageResult> getCurrencyPriceList(@RequestParam Integer page,@RequestParam Integer pageSize){
        return currencyTypeService.getCurrencyPriceList(page,pageSize);
    }


    /**
     * 后台管理系统 - 首页 - 加密数字版数据报表
     * @return
     */
    @GetMapping("getYesterdayStatistics")
    public ResponseData getYesterdayStatistics(){
        return mongoDBService.getYesterdayStatistics();
    }

    /**
//     * 后台管理系统 - 首页统计 - 接收发送统计 - 历史接收币价值排名 - 当前用户总价值数据获取
     * 后台管理系统 - 会员管理 - 团队管理 - 团队虚拟资产统计数据获取
     * @param teamParamerMap 会员或团队名下对应的地址信息
     * @return
     */
    @PostMapping("getTeamTotalAsset")
    public ResponseData getTeamTotalAsset(@RequestBody Map<String , List<TransactionAddressVO>> teamParamerMap){
        return mongoDBService.getTeamTotalAsset(teamParamerMap);
    }

    /**
     * 后台管理系统 - 会员管理 - 会员资金管理 - 会员名下所有公链 symbol 数字货币余额获取
     * @return
     */
    @PostMapping("getMemberAssetManagement")
    public ResponseData getMemberAssetManagement(@RequestBody Map<String , List<TransactionAddressVO>> teamParamerMap){
        return mongoDBService.getMemberAssetManagement(teamParamerMap);
    }


    /**
     * 后台管理系统 - 首页统计 - 接收发送统计 - 历史接收币价值排名 - 当前用户总价值数据和历史接收币总价值获取
     * @param vo 查询参数
     * @return
     */
    @PostMapping("getUserTotalReceieValue")
    public ResponseData getUserTotalReceieValue(@RequestBody HistoricalValueVO vo){
        return mongoDBService.getUserTotalReceieValue(vo);
    }


    /**
     * 后台-首页统计-基础数据看板-今日做兑换业务的会员总数据
     * @return
     */
    @GetMapping("getToDayExchangeUser")
    public ResponseData getToDayExchangeUser(){
        return exchangeTransactionService.getToDayExchangeUser();
    }



    /**
     * 后台-首页统计-基础数据看板-今日兑换得到的枚数
     * @return
     */
    @GetMapping("getExchangeSymbolNumber")
    public ResponseData getExchangeSymbolNumber(){
        return exchangeTransactionService.getExchangeSymbolNumber();
    }


    /**
     * 后台-首页统计-基础数据看板-今日做兑换业务的笔数
     * @return
     */
    @GetMapping("getTransactionCount")
    public ResponseData getTransactionCount(){
        return exchangeTransactionService.getTransactionCount();
    }



    @GetMapping("getAddressByNet")
    public ResponseData getAddressByNet(@RequestParam String net, @RequestParam Integer page, @RequestParam Integer pageSize){
        return mongoDBService.getAddressByNet(net,page,pageSize);
    }

    @PostMapping("getBalanceByAddress")
    public ResponseData getBalanceByAddress(@RequestBody BalanceManagementVO balanceManagementVO){
        return mongoDBService.getBalanceByAddress(balanceManagementVO);
    }







}
