package com.block.chain.controller;


import com.alibaba.fastjson.JSONObject;
import com.block.chain.service.*;
import com.block.chain.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Statement configuration controller
 * @author michael
 * @date 2022/10/21
 */
@RestController
@RequestMapping("/statement")
public class StatementController {

    @Autowired
    private PrimaryAddressService primaryAddressService;

    @Autowired
    private StatisticInfoService statisticInfoService;

    @Autowired
    private StatisticWalletService statisticWalletService;

    @Autowired
    private StatisticAddressService statisticAddressService;

    /**
     * 根据起止时间获取统计数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    @GetMapping("/getStatisticInfoList")
    public ResponseData getStatisticInfoList(@RequestParam Long startTime ,@RequestParam Long endTime){
        return statisticInfoService.getStatisticInfoList(startTime,endTime);
    }

    /**
     * 统计
     * @return
     */
    @GetMapping("getStatementInfo")
    public ResponseData getStatementInfo(){
        return primaryAddressService.getStatementInfo();
    }

    /**
     * 公司钱包数据统计录入
     * @param json 钱包数据统计信息
     * @return
     */
    @PostMapping("/saveWalletInfo")
    public ResponseData saveWalletInfo(@RequestBody JSONObject json){
        return statisticWalletService.saveWalletInfo(json);
    }


    /**
     * 获取所有公链下统计信息-包含总价值统计
     * @return
     */
    @CrossOrigin
    @GetMapping("getWalletInfoList")
    public ResponseData getWalletInfoList(){
        return statisticWalletService.getWalletInfoList();
    }


    /**
     * 获取钱包地址下统计信息
     * @param address
     * @return
     */
    @CrossOrigin
    @GetMapping("/getStatisticAddressList")
    public ResponseData getStatisticAddressList(@RequestParam String net,@RequestParam String address){
       return  statisticAddressService.getStatisticAddressList(net,address);
    }


    /**
     * 所有公司地址下今日划转，提现数量统计以及历史划转，提现数量统计
     * @return
     */
    @CrossOrigin
    @GetMapping("/getHistoryStatisticInfo")
    public ResponseData getHistoryStatisticInfo(){
        return  statisticWalletService.getHistoryStatisticInfo();
    }
}
