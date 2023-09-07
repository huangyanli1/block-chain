package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.StatisticAddressEntity;
import com.block.chain.entity.StatisticWalletEntity;
import com.block.chain.entity.WalletTransactionEntity;
import com.block.chain.mapper.StatisticAddressMapper;
import com.block.chain.mapper.StatisticWalletMapper;
import com.block.chain.mapper.WalletTransactionMapper;
import com.block.chain.service.StatisticAddressService;
import com.block.chain.service.StatisticWalletService;
import com.block.chain.utils.ResponseData;
import com.block.chain.utils.redis.RedisKeyName;
import com.block.chain.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


@Slf4j
@Service("statisticWalletService")
public class StatisticWalletServiceImpl extends ServiceImpl<StatisticWalletMapper, StatisticWalletEntity> implements StatisticWalletService {

    @Autowired
    private StatisticWalletMapper statisticWalletMapper;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private StatisticAddressMapper statisticAddressMapper;

    @Autowired
    private StatisticAddressService statisticAddressService;

    @Autowired
    RedisUtil redisUtil;

    /**
     * 获取所有公链下统计信息-包含总价值统计
     * @return
     */
    public ResponseData getWalletInfoList(){
        List<StatisticWalletEntity> list = statisticWalletMapper.getWalletInfoList();
        return ResponseData.ok(list);
    }

    /**
     * 所有公司地址下今日划转，提现数量统计以及历史划转，提现数量统计
     * @return
     */
    public ResponseData getHistoryStatisticInfo(){
        JSONObject json = new JSONObject();
        String historyStatisticInfo = redisUtil.getString(RedisKeyName.HistoryStatisticInfo);
        if(StringUtils.isNotEmpty(historyStatisticInfo)){
            json = JSONObject.parseObject(historyStatisticInfo);
        }
        return ResponseData.ok(json);
    }

    /**
     * 公司钱包数据统计录入
     * @param json 钱包数据统计信息
     * @return
     */
    @Async
    public ResponseData saveWalletInfo(JSONObject json){
        JSONObject statistics = JSONObject.parseObject(JSONObject.toJSONString(json.get("statistics")));
        //公链统计拆分和录入
        //获取所有公链总价值之和
        BigDecimal totalValue = statistics.getBigDecimal("totalValue") == null ? new BigDecimal(0) : statistics.getBigDecimal("totalValue");
        //获取所有主币统计信息
        JSONArray mainNet = JSONArray.parseArray(JSONObject.toJSONString(statistics.get("mainNet")));

        List<StatisticWalletEntity> walletList = this.getWalletList(totalValue,mainNet);
        //全量统计，所以可以先删除全部的统计记录，在全部录入
        int size = statisticWalletMapper.remove();
        this.saveBatch(walletList);

        //地址相关统计记录拆分和录入
        //获取所有地址统计信息
        JSONArray balance = JSONArray.parseArray(JSONObject.toJSONString(json.get("balance")));
        List<StatisticAddressEntity> addressList = this.getAddressList(balance);
        //全量统计，所以可以先删除全部的统计记录，在全部录入
        int number =statisticAddressMapper.remove();
        statisticAddressService.saveBatch(addressList);

        //获取所有公司地址信息 - 用来做所有公司地址下今日划转，提现数量统计以及历史划转，提现数量统计
        JSONArray companyAddress = JSONArray.parseArray(JSONObject.toJSONString(json.get("companyAddress")));
        Map<String , Object> historyStatisticInfo = getHistoryStatistic(companyAddress);
        try {
            String historyStatistic = JSONObject.toJSONString(historyStatisticInfo);
            redisUtil.set(RedisKeyName.HistoryStatisticInfo, historyStatistic, 86400);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseData.ok("统计信息录入成功");
    }

    /**
     * 获取所有公司地址信息 - 用来做所有公司地址下今日划转，提现数量统计以及历史划转，提现数量统计
     * @param companyAddress 所有公司地址信息
     * @return
     */
    public Map<String , Object> getHistoryStatistic(JSONArray companyAddress){
        log.info("所有公司地址信息"+JSONObject.toJSONString(companyAddress));
        Map<String,Object> map = new HashMap<>();
        List<String> addressList = new ArrayList<>();
        companyAddress.stream().forEach(x->{
            StatisticAddressEntity entity = new StatisticAddressEntity();
            JSONObject json=(JSONObject) x;
            //公链类型
            String net = json.get("net") == null ? "":json.get("net").toString();
            //公司钱包地址
            String address = json.get("address") == null ? "":json.get("address").toString();
            addressList.add(address);
        });
        if(CollectionUtils.isEmpty(addressList)){
            return map;
        }
        Long startTime = this.getStartTime();
        Long endTime = this.getEndTime();
        Long yesterdayEndTime = getYesterdayEndTime();
        //获取今日所有入账交易记录
        List<WalletTransactionEntity>  entriesList = walletTransactionMapper.getEntriesTransactionList(addressList,startTime,endTime,null);
        //获取历史所有入账交易记录
        List<WalletTransactionEntity>  entriesHistoryList = walletTransactionMapper.getEntriesTransactionList(addressList,null,yesterdayEndTime,null);
        Integer entriesNumber = entriesList.size();//今日入账笔数
        BigDecimal entriesCurrencyNumber = this.getTotalMoney(entriesList);//今日入账金额
        BigDecimal entriesHistoryNumber = this.getTotalMoney(entriesHistoryList);//历史入账金额
        //获取今日日提现交易记录和交易币数量
        List<WalletTransactionEntity>  walletList = walletTransactionMapper.getWithdrawalTransactionList(addressList,startTime,endTime,null);
        List<WalletTransactionEntity>  walletHistoryList = walletTransactionMapper.getWithdrawalTransactionList(addressList,null,yesterdayEndTime,null);
        Integer withdrawalNumber = walletList.size();//今日提现笔数
        BigDecimal withdrawalCurrencyNumber = this.getTotalMoney(walletList);//今日提现金额
        BigDecimal withdrawalHistoryNumber = this.getTotalMoney(walletHistoryList);//历史提现金额
        map.put("entriesNumber",entriesNumber);
        map.put("entriesCurrencyNumber",entriesCurrencyNumber);
        map.put("entriesHistoryNumber",entriesHistoryNumber);
        map.put("withdrawalNumber",withdrawalNumber);
        map.put("withdrawalCurrencyNumber",withdrawalCurrencyNumber);
        map.put("withdrawalHistoryNumber",withdrawalHistoryNumber);
        return map;
    }

    /**
     * 处理所有的地址统计记录
     * @param balance 地址统计信息
     * @return
     */
    public List<StatisticAddressEntity> getAddressList(JSONArray balance){
        List<StatisticAddressEntity> list = new ArrayList<>();
        balance.stream().forEach(x->{
            StatisticAddressEntity entity = new StatisticAddressEntity();
            JSONObject json=(JSONObject) x;
            //公链类型
            String net = json.get("net") == null ? "":json.get("net").toString();
            //钱包地址
            String address = json.get("address") == null ? "":json.get("address").toString();
            //币的总个数
            BigDecimal balanceNum = json.getBigDecimal("balanceNum") == null ? new BigDecimal(0) : json.getBigDecimal("balanceNum");
            //币种符号
            String symbol = json.get("symbol") == null ? "":json.get("symbol").toString();
            //币类型
            Integer coinType = json.get("coinType") == null? null : Integer.valueOf(json.get("coinType").toString());
            //今天的开始时间和结束时间 - 格林
            Long startTime = this.getStartTime();
            Long endTime = this.getEndTime();
            List<String> addressList = new ArrayList<>();
            addressList.add(address);
            //获取今日所有入账交易记录和交易币数量
            List<WalletTransactionEntity>  entriesList = walletTransactionMapper.getEntriesTransactionList(addressList,startTime,endTime,symbol);
            Integer entriesNumber = entriesList.size();
            BigDecimal entriesCurrencyNumber = this.getCurrencyNumber(entriesList);
            //获取今日日提现交易记录和交易币数量
            List<WalletTransactionEntity>  walletList = walletTransactionMapper.getWithdrawalTransactionList(addressList,startTime,endTime,symbol);
            Integer withdrawalNumber = walletList.size();
            BigDecimal withdrawalCurrencyNumber = this.getCurrencyNumber(walletList);
            entity.setNet(net);
            entity.setAddress(address);
            entity.setCurrencyNumber(balanceNum);
            entity.setSymbol(symbol);
            entity.setCoinType(coinType);
            entity.setEntriesNumber(entriesNumber);
            entity.setEntriesCurrencyNumber(entriesCurrencyNumber);
            entity.setWithdrawalNumber(withdrawalNumber);
            entity.setWithdrawalCurrencyNumber(withdrawalCurrencyNumber);
            entity.setCreateTime(new Date());
            list.add(entity);
        });
        return list;
    }


    /**
     * 计算昨日入账，和提现数量 - 公用
     * @return
     */
    public BigDecimal getCurrencyNumber(List<WalletTransactionEntity> list){
        BigDecimal balance = new BigDecimal(0);
        for(WalletTransactionEntity entity : list){
            BigDecimal transactionValue = entity.getTransactionValue();
            balance = balance.add(transactionValue);
        }
        return balance;
    }

    /**
     * 计算入账，和提现总价值 - 即换成美元 - 公用
     * @return
     */
    public BigDecimal getTotalMoney(List<WalletTransactionEntity> list){
        BigDecimal balance = new BigDecimal(0);
        for(WalletTransactionEntity entity : list){
            BigDecimal coinValue = entity.getCoinValue();
            if(coinValue != null){
                balance = balance.add(coinValue);
            }
        }
        return balance;
    }


    //获取当天的开始时间
    public  Long getStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        todayStart.add(Calendar.HOUR_OF_DAY, -8);
        return todayStart.getTimeInMillis();
    }

    //获取当天的结束时间
    public  Long getEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MILLISECOND, 999);
        todayEnd.add(Calendar.HOUR_OF_DAY, -8);
        return todayEnd.getTimeInMillis();
    }
    //获取昨日的结束时间
    public  Long getYesterdayEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MILLISECOND, 999);
        todayEnd.add(Calendar.DAY_OF_MONTH, -1);
        todayEnd.add(Calendar.HOUR_OF_DAY, -8);
        return todayEnd.getTimeInMillis();
    }


    /**
     * 处理并获取所有的公链统计记录
     * @param totalValue 所有公链总价值之和
     * @param mainNet 公链统计数据
     * @return
     */
    public List<StatisticWalletEntity> getWalletList(BigDecimal totalValue ,JSONArray mainNet){
        List<StatisticWalletEntity> list = new ArrayList<>();
        mainNet.stream().forEach(x->{
            StatisticWalletEntity entity = new StatisticWalletEntity();
            JSONObject json=(JSONObject) x;
            //公链类型
            String net = json.get("net") == null ? "":json.get("net").toString();
            //地址个数
            Integer addressCount = json.get("addressCount") == null? null : Integer.valueOf(json.get("addressCount").toString());
            //币的总个数
            BigDecimal amount = json.getBigDecimal("amount") == null ? new BigDecimal(0) : json.getBigDecimal("amount");
            //币价
            BigDecimal price = json.getBigDecimal("price") == null ? new BigDecimal(0) : json.getBigDecimal("price");
            //币的总价值
            BigDecimal value = json.getBigDecimal("value") == null ? new BigDecimal(0) : json.getBigDecimal("value");
            entity.setNet(net);
            entity.setAddressNumber(addressCount);
            entity.setPrimaryCurrencyNumber(amount);
            entity.setCurrentPrice(price);
            entity.setTotalValue(value);
            //统计类型(1:公链统计数据 2：总价值统计 公链统计所有的总价值之和)
            entity.setStatisticStatus(1);
            entity.setCreateTime(new Date());
            list.add(entity);
        });
        StatisticWalletEntity walletEntity = new StatisticWalletEntity();
        walletEntity.setTotalValue(totalValue);
        walletEntity.setStatisticStatus(2);
        walletEntity.setCreateTime(new Date());
        list.add(walletEntity);
       return list;

    }




}