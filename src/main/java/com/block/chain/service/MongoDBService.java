package com.block.chain.service;


import com.block.chain.utils.ResponseData;
import com.block.chain.vo.TransactionAddressVO;
import com.block.chain.vo.management.BalanceManagementVO;
import com.block.chain.vo.management.HistoricalValueVO;

import java.util.List;
import java.util.Map;

/**
 * 钱包交易
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-17 14:51:23
 */
public interface MongoDBService {

    /**
     * 后台管理系统-首页统计-接收统计
     * @return
     */
    public ResponseData currencyReceiveStatistics();


    /**
     * 后台管理系统-首页统计-接收统计数据查询- 当天数据
     * @return
     */
    public ResponseData getReceiveStatisticsList();

    /**
     * 后台管理系统-首页统计-发送统计
     * @return
     */
    public ResponseData currencySendingStatistics();

    /**
     * 后台管理系统-首页统计-发送统计数据查询 -当天数据
     * @return
     */
    public ResponseData getSendingStatisticsList();

    /**
     * 后台管理系统-首页统计接收统计-钱包接收币笔数趋势图(币种和net查询)
     * @return
     */
    public ResponseData getReceiveStatistics(String net,String symbol);

    /**
     * 后台管理系统-首页统计发送统计-钱包发送币笔数趋势图(币种和net查询)
     * @return
     */
    public ResponseData getSendingStatistics(String net,String  symbol);

    /**
     * 会员管理 - 会员基础信息 - 加密数字资产管理
     * @return
     */
    public ResponseData getMemberAssets(List<TransactionAddressVO> addressList);

    /**
     * 后台管理系统-首页统计-财务收款总额，笔数统计 - 主要统计公司地址相关数据
     * @return
     */
    public ResponseData currencyCompanyReceiveStatistics();

    /**
     * 后台管理系统-首页统计接收统计-财务收款总额，笔数数据查询 - 时间段（日，周，月，年）
     * @return
     */
    public ResponseData getCompanyReceiveStatistics(Integer type);

    /**
     * 后台管理系统 - 首页 - 加密数字版数据报表
     * @return
     */
    public ResponseData getYesterdayStatistics();

    /**
     * 后台管理系统 - 首页统计 - 接收发送统计 - 历史接收币价值排名 - 当前用户总价值数据获取
     * 后台管理系统 - 会员管理 - 团队管理 - 团队虚拟资产统计数据获取
     * @param teamParamerMap 会员或团队名下对应的地址信息
     * @return
     */
    public ResponseData getTeamTotalAsset(Map<String , List<TransactionAddressVO>> teamParamerMap);


    /**
     * 后台管理系统 - 会员管理 - 会员资金管理 - 会员名下所有公链 symbol 数字货币余额获取
     * @return
     */
    public ResponseData getMemberAssetManagement(Map<String , List<TransactionAddressVO>> teamParamerMap);

    /**
     * 批量获取获取公链地址下余额数据 - 地址余额记录入MongoDB库后
     * @param addressList 查询地址信息
     * @return
     */
    public ResponseData getMongoAddressBalanceList(List<TransactionAddressVO> addressList);

    /**
     * 后台管理系统 - 首页统计 - 接收发送统计 - 历史接收币价值排名 - 当前用户总价值数据和历史接收币总价值获取
     * @return
     */
    public ResponseData getUserTotalReceieValue(HistoricalValueVO vo);


    public ResponseData getAddressByNet(String net, Integer page, Integer pageSize);
    public ResponseData getBalanceByAddress(BalanceManagementVO manageVO);


    }

