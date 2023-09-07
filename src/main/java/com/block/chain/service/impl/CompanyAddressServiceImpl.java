package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CompanyAddressEntity;
import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.entity.mongo.TRX;
import com.block.chain.mapper.CompanyAddressMapper;
import com.block.chain.mapper.CurrencyPriceMapper;
import com.block.chain.mapper.TransactionBtcInfoMapper;
import com.block.chain.service.CompanyAddressService;
import com.block.chain.utils.CommonService;
import com.block.chain.utils.Constant;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.CompanyAddressVO;
import com.block.chain.vo.TransactionBtcInfoVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


@Service("companyAddressService")
public class CompanyAddressServiceImpl extends ServiceImpl<CompanyAddressMapper, CompanyAddressEntity> implements CompanyAddressService {


    @Autowired
    private CompanyAddressMapper companyAddressMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CurrencyPriceMapper currencyPriceMapper;

    @Autowired
    private TransactionBtcInfoMapper transactionBtcInfoMapper;

    @Autowired
    public CommonService commonService;



    public ResponseData saveCompanyAddress(List<CompanyAddressEntity> list){
        BigDecimal balance = new BigDecimal(0);
        for(CompanyAddressEntity entity : list){
            String address = entity.getAddress();
            String net = entity.getNet();
            Map<String, Object> columnMap = new HashMap<String , Object>();
            columnMap.put("address", address);
            columnMap.put("net", net);
            List<CompanyAddressEntity>  addressList= companyAddressMapper.selectByMap(columnMap);
            if(CollectionUtils.isEmpty(addressList)){
                entity.setBalance(balance);
                companyAddressMapper.insert(entity);
            }
        }
        return ResponseData.ok("地址录入成功");
    }

    public ResponseData isCompanyAddress(String address ,String net){
        Boolean isCompanyAddress = false;
        if(StringUtils.isEmpty(address)||StringUtils.isEmpty(net)){
            return ResponseData.fail("地址或公链信息缺失！");
        }

        Map<String, Object> columnMap = new HashMap<String , Object>();
        columnMap.put("net", net);
        List<CompanyAddressEntity>  addressList= companyAddressMapper.selectByMap(columnMap);
        if(CollectionUtils.isNotEmpty(addressList)){
            for(CompanyAddressEntity entity : addressList){
                String ads = entity.getAddress();
                String comNet =entity.getNet();
                if(Constant.BTC.equals(comNet)||Constant.TRX.equals(comNet)){
                    if(address.equals(ads)){
                        isCompanyAddress = true ;
                    }
                }else{
                    String newAds = ads.toLowerCase();
                    String newAddress = address.toLowerCase();
                    if(newAddress.equals(newAds)){
                        isCompanyAddress = true ;
                    }
                }
            }
        }
        return ResponseData.ok(isCompanyAddress);
    }

    public ResponseData getCompanyAddressList(List<CompanyAddressVO> list){
        Map<Long,CompanyAddressVO> resultMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(list)){
            Map<String,BigDecimal> priceMap = getPriceMap();
            for(CompanyAddressVO entity : list){
                CompanyAddressVO vo = new CompanyAddressVO();
                String address = entity.getAddress();
                String net = entity.getNet();
                String symbol = entity.getSymbol();
                BigDecimal price = priceMap.get(symbol)  == null ? new BigDecimal(0) : priceMap.get(symbol);
                String priceChar = price.toPlainString();

                Query query = new Query();
                query.addCriteria(Criteria.where("address").is(address));
                TRX trx = this.mongoTemplate.findOne(query, TRX.class,net);
                BeanUtils.copyProperties(entity, vo);
                vo.setBalance("0");
                vo.setPrice(priceChar);
                vo.setTotalValue("0");
                if(trx != null){
                    if(symbol.equals(net)){
                        BigDecimal primaryBalance = trx.getBalance() == null ? new BigDecimal(0) :trx.getBalance();
                        String balanceChar = primaryBalance.stripTrailingZeros().toPlainString();
                        BigDecimal totalValue = primaryBalance.multiply(price);
                        String totalValueChar = totalValue.stripTrailingZeros().toPlainString();
                        vo.setBalance(balanceChar);
                        vo.setPrice(priceChar);
                        vo.setTotalValue(totalValueChar);
                    }else{
                        List<Map<String,Object>> tokens = trx.getTokens();
                        if(CollectionUtils.isNotEmpty(tokens)){
                            for(Map<String , Object> map : tokens){
                                String tokenSymbol = String.valueOf(map.get("symbol")) == null ? "": String.valueOf(map.get("symbol"));
                                if(symbol.equals(tokenSymbol)){
                                    BigDecimal balance = String.valueOf(map.get("balance")) == null ? new BigDecimal(0) :new BigDecimal(String.valueOf(map.get("balance")));
                                    String tokenBalance = balance.stripTrailingZeros().toPlainString();
                                    BigDecimal totalValue = balance.multiply(price);
                                    String totalValueChar = totalValue.stripTrailingZeros().toPlainString();
                                    vo.setBalance(tokenBalance);
                                    vo.setPrice(priceChar);
                                    vo.setTotalValue(totalValueChar);
                                }
                            }
                        }
                    }
                }
                resultMap.put(vo.getId(),vo);
            }
        }
        return  ResponseData.ok(resultMap);
    }

    public Map<String,BigDecimal> getPriceMap(){
        Map<String,BigDecimal> priceMap = new HashMap<>();
        List<CurrencyPriceEntity> list = currencyPriceMapper.getCurrencyPriceList(null);
        for(CurrencyPriceEntity entity : list){
            String currencySymbol = entity.getCurrencySymbol();
            BigDecimal priceUsd = entity.getPriceUsd();
            priceMap.put(currencySymbol,priceUsd);
        }
        return priceMap;
    }

    public ResponseData companyWalletManagement(){
        Map<String,List<String>> map = new HashMap<>();
        List<CompanyAddressEntity> list=companyAddressMapper.getAllCompanyAddress();
        if(CollectionUtils.isNotEmpty(list)){
            for(CompanyAddressEntity entity : list){
                String net = entity.getNet();
                String address = entity.getAddress();
                if(map.containsKey(net)){
                    List<String> addressList = map.get(net);
                    addressList.add(address);
                    map.put(net,addressList);
                }else{
                    List<String> addressList = new ArrayList<>();
                    addressList.add(address);
                    map.put(net,addressList);
                }

            }

        }

           return ResponseData.ok("");
    }

    public ResponseData getQuantityStatistics(List<CompanyAddressVO> list){
        JSONObject json = new JSONObject();
        Map<String ,List<String>> map = new HashMap<>();
        if(CollectionUtils.isNotEmpty(list)){
            for(CompanyAddressVO vo : list){
                String net = vo.getNet();
                String address = vo.getAddress();
                if(map.containsKey(net)){
                    List<String> addressList = map.get(net);
                    addressList.add(address);
                    map.put(net,addressList);
                }else {
                    List<String> addressList = new ArrayList<>();
                    addressList.add(address);
                    map.put(net,addressList);
                }
            }
        }
        Long startTime = this.getStartTime();
        Long endTime = this.getEndTime();
        int entySum=0;//今日入账笔数
        BigDecimal entryTotal = new BigDecimal(0); //今日入账数量
        Integer transferSum = 0;//今日出账笔数
        BigDecimal transferTotal = new BigDecimal(0); //今日出账数量
        //分别查询对应公链下今日的入账，出账交易数量进行统计
        for (Map.Entry<String, List<String>> entry:map.entrySet()){
            String key = entry.getKey();
            List<String> value = entry.getValue();
            String tableName = "transaction_" + key + "_info";
            List<String> addressList = value;
            tableName = tableName.toLowerCase();
            if(StringUtils.isNotEmpty(tableName)){
                Boolean isHave =commonService.isHaveTable(tableName);
                if(!isHave){
                    break;
                }
            }
            for(String address : addressList){
                List<TransactionBtcInfoVO> entList=transactionBtcInfoMapper.getEntriesStatisticsTransactionList(tableName,address,startTime,endTime);
                List<TransactionBtcInfoVO> witList = transactionBtcInfoMapper.getWithdrawalStatisticsTransactionList(tableName,address,startTime,endTime);
                if(CollectionUtils.isNotEmpty(entList)){
                    int counte = entList.size();
                    entySum = entySum + counte;
                    for(TransactionBtcInfoVO vo : entList){
                        BigDecimal diff = vo.getDiff() == null ? new BigDecimal(0) : vo.getDiff();
                        entryTotal = entryTotal.add(diff);
                    }
                }

                if(CollectionUtils.isNotEmpty(witList)){
                    int countw = witList.size();
                    transferSum =transferSum + countw;
                    for(TransactionBtcInfoVO vo :witList){
                        BigDecimal diff = vo.getDiff() == null ? new BigDecimal(0) : vo.getDiff();
                        transferTotal = transferTotal.add(diff);
                    }
                }
//                int counte = transactionBtcInfoMapper.getEntriesStatisticsTransaction(tableName,address,startTime,endTime);
//                int countw = transactionBtcInfoMapper.getWithdrawalStatisticsTransaction(tableName,address,startTime,endTime);
            }
        }
        json.put("entySum",entySum);//今日入账笔数
        json.put("entryTotal",entryTotal);//今日入账数量
        json.put("transferSum",transferSum);//今日出账笔数
        json.put("transferTotal",transferTotal);//今日出账数量
        json.put("abnormalSum",0);//交易异常

        return ResponseData.ok(json);
    }


    public  Long getStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        todayStart.add(Calendar.HOUR_OF_DAY, -8);
        return todayStart.getTimeInMillis()/1000;
    }
    public  Long getEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MILLISECOND, 999);
        todayEnd.add(Calendar.HOUR_OF_DAY, -8);
        return todayEnd.getTimeInMillis()/1000;
    }

}