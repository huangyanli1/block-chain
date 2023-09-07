package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.*;
import com.block.chain.mapper.CurrencyTypeMapper;
import com.block.chain.mapper.ExchangeTransactionMapper;
import com.block.chain.service.ExchangeTransactionService;
import com.block.chain.service.MessagePushFailService;
import com.block.chain.utils.Constant;
import com.block.chain.utils.DatesUtil;
import com.block.chain.utils.PageUtils;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.convert.HistoryExchangeTransactionVO;
import com.block.chain.vo.convert.SwappedVO;
import com.block.chain.vo.convert.WalletCurrencyTransactionVO;
import com.block.chain.vo.management.ExchangeManagementVO;
import com.block.chain.vo.management.TransactionParamerVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.client.RestTemplate;


@Service("exchangeTransactionService")
public class ExchangeTransactionServiceImpl extends ServiceImpl<ExchangeTransactionMapper, ExchangeTransactionEntity> implements ExchangeTransactionService {


    @Autowired
    private ExchangeTransactionMapper exchangeTransactionMapper;

    @Autowired
    private CurrencyTypeMapper currencyTypeMapper;


    @Value("${BUSSINESS.SelectMemberByAddressUrl}")
    private String selectMemberByAddressUrl;

    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private MessagePushFailService messagePushFailService;




    /**
     * 兑换列表查询接口  - exchange now
     * @param vo 查询参数
     * @return
     */
    public ResponseData getCurrencyTransactionList(TransactionParamerVO vo){
//        List<WalletCurrencyTransactionVO> voList = new ArrayList<>();
//        Integer currPage = vo.getPage();
//        Integer page = (vo.getPage()-1)*vo.getPageSize();
//        vo.setPage(page);
//        if(CollectionUtils.isEmpty(vo.getOrderList())){
//            vo.setOrderList(null);
//        }
//        List<ExchangeTransactionEntity> list = exchangeTransactionMapper.getTransactionList(vo);
//        if(CollectionUtils.isNotEmpty(list)){
//            for(ExchangeTransactionEntity entity : list){
//                WalletCurrencyTransactionVO tran = new WalletCurrencyTransactionVO();
//                BeanUtils.copyProperties(entity, tran);
//                String outAmount = entity.getOutAmount() == null ? "" : entity.getOutAmount().stripTrailingZeros().toPlainString();
//                tran.setOutAmount(outAmount);
//                voList.add(tran);
//            }
//        }
//        int count = walletCurrencyTransactionMapper.getCurrencyTransactionCount(vo);
//        PageUtils pageUtils = new PageUtils(voList,count,vo.getPageSize(),currPage);
//        return ResponseData.ok(pageUtils);
        return ResponseData.ok("");
    }


    public ResponseData decryptTransactionInfo(String s){
        processCharacters(s);
        return ResponseData.ok("");

    }


    public void processCharacters(String data){
        SwappedVO vo = new SwappedVO();
        int len = data.length();
        Integer number = 0;
        for (int i = len; i >= 0; i -= 64) {
            number++;
            Long amount = null;
            String address = "";
            if(i >= 64){
                int startLength = i- 64;
                String chunk = data.substring(startLength,i);
                if(number <= 2){
                    amount = Long.parseLong(chunk,16);
                    System.out.println(amount);
                }else{
                    address = chunk.substring(chunk.length()-40,chunk.length());
                    address = "0x"+address;
                    System.out.println(address);
                }
            }
            if(number == 1){
                 vo.setReturnAmount(amount);
            }else if(number == 2){
                 vo.setSpentAmount(amount);
            }else if(number == 3){
                 vo.setDstReceiver(address);
            }else if(number == 4){
                vo.setDstToken(address);
            }else if(number == 5){
                vo.setSrcToken(address);
            }else if(number == 6){
                vo.setSender(address);
            }
        }

        String s="";
    }



    public static Long convertNum(String str)
    {
        String[] c={"A","B","C","D","E","F"};
        HashMap<String,Integer> hashmap= new HashMap<>();
        for(int i=0; i<=9;i++)
        {
            hashmap.put(i+"",i);
        }
        for(int j=10;j<=15;j++)
        {
            hashmap.put(c[j-10],j);
        }

        String[] st=new String[str.length()];
        for(int i=0;i<=str.length()-1;i++)
        {
            st[i]=str.substring(i,i+1);
        }

        int num=0;
        for(int i=2;i<=st.length-1;i++)
        //这里需要注意，如果输入是类似"1A"没有标识的16进制数，i的值从0开始
        //如果是"0x1A"这样有标识符的16进制，则在计算时，需要截掉0x这两位标识，i从2开始
        {
            num += hashmap.get(st[i]) * Math.pow(16,st.length-1-i);
        }
        return Long.valueOf(num);
    }




    /**
     * 兑换列表查询接口  - exchange now
     * @param vo 查询参数
     * @return
     */
    public ResponseData getHistoryExchangeTransactionList(TransactionParamerVO vo){
        List<HistoryExchangeTransactionVO> voList = new ArrayList<>();
        Integer currPage = vo.getPage();
        Integer page = (vo.getPage()-1)*vo.getPageSize();
        vo.setPage(page);
        if(CollectionUtils.isEmpty(vo.getAddress())){
            vo.setAddress(null);
        }
        if(vo.getStartTime() != null&& vo.getEndTime() != null){
            Long startTime = vo.getStartTime()*1000;
            Long endTime = vo.getEndTime()*1000;
            vo.setStartTime(startTime);
            vo.setEndTime(endTime);
        }

        List<ExchangeTransactionEntity> list = exchangeTransactionMapper.getHistoryExchangeTransactionList(vo);
        if(CollectionUtils.isNotEmpty(list)){
            for(ExchangeTransactionEntity entity : list){
                HistoryExchangeTransactionVO tran = new HistoryExchangeTransactionVO();
                BeanUtils.copyProperties(entity, tran);
                String fromAmount = entity.getFromAmount() == null ? "" : entity.getFromAmount().stripTrailingZeros().toPlainString();
                String toAmount = entity.getToAmount() == null ? "" : entity.getToAmount().stripTrailingZeros().toPlainString();
                tran.setFromAmount(fromAmount);
                tran.setToAmount(toAmount);
                tran = this.getTransactionVO(tran);
                voList.add(tran);
            }
        }
        int count = exchangeTransactionMapper.getHistoryExchangeTransactionCount(vo);
        PageUtils pageUtils = new PageUtils(voList,count,vo.getPageSize(),currPage);
        return ResponseData.ok(pageUtils);
    }


    /**
     * 根据币种获取币种信息并填充URL和是否主币
     * @param vo
     * @return
     */
    public HistoryExchangeTransactionVO getTransactionVO(HistoryExchangeTransactionVO vo){

        try {
            String fromCurrency =vo.getFromCurrency() == null ? "" : vo.getFromCurrency();
            String toCurrency = vo.getToCurrency() == null ? "" : vo.getToCurrency();
            String fromNetWork = vo.getFromNetwork() == null ? "" : vo.getFromNetwork();
            String toNetWork = vo.getToNetwork() == null ? "" : vo.getToNetwork();
            CurrencyTypeEntity entity  = currencyTypeMapper.getCurrencyInfo(fromNetWork,fromCurrency);
            CurrencyTypeEntity toEntity  = currencyTypeMapper.getCurrencyInfo(toNetWork,toCurrency);

            //放入扣除币对应的logo和链logo，并判断是否为主币 0 不是主币  1 是主币
            if(entity != null && entity.getCoinType() == 2){
                vo.setFromCurrencyUrl(entity.getLogoUrl());
                CurrencyTypeEntity currency  = currencyTypeMapper.getMainCurrencyInfo(fromNetWork);
                vo.setFromNetworkUrl(currency.getLogoUrl());
                vo.setIsMainCurrencyFrom(0);
            }else{
                vo.setFromCurrencyUrl(entity.getLogoUrl());
                vo.setIsMainCurrencyFrom(1);
            }

            //放入得到币对应的logo和链logo，并判断是否为主币 0 不是主币  1 是主币
            if(toEntity != null && toEntity.getCoinType() == 2){
                vo.setToCurrencyUrl(toEntity.getLogoUrl());
                CurrencyTypeEntity currency  = currencyTypeMapper.getMainCurrencyInfo(toNetWork);
                vo.setToNetworkUrl(currency.getLogoUrl());
                vo.setIsMainCurrencyTo(0);
            }else{
                vo.setToCurrencyUrl(toEntity.getLogoUrl());
                vo.setIsMainCurrencyTo(1);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("根据币种获取币种信息并填充URL和是否主币"+ e);
            return null;
        }
        return vo;
    }

    /**
     * 根据订单Id获取兑换详情
     * @param orderId
     * @return
     */
    public ResponseData getExchangeTransactionByOrderId(String orderId){
        HistoryExchangeTransactionVO vo = new HistoryExchangeTransactionVO();
        if(StringUtils.isNotEmpty(orderId)){
            ExchangeTransactionEntity entity = exchangeTransactionMapper.getExchangeTransactionByOrderId(orderId);
            BeanUtils.copyProperties(entity, vo);
            String fromAmount = entity.getFromAmount() == null ? "" : entity.getFromAmount().stripTrailingZeros().toPlainString();
            String toAmount = entity.getToAmount() == null ? "" : entity.getToAmount().stripTrailingZeros().toPlainString();
            vo.setFromAmount(fromAmount);
            vo.setToAmount(toAmount);
        }
        return ResponseData.ok(vo);
    }



    /**
     * 根据订单Id修改兑换订单状态
     * @param orderId
     * @return
     */
    public ResponseData updateTransactionByOrderId(String orderId){
        ExchangeTransactionEntity entity =new ExchangeTransactionEntity();
        if(StringUtils.isNotEmpty(orderId)){
            entity.setExchangeOrderId(orderId);
            exchangeTransactionMapper.updateTransactionByOrderId(entity);
            try{
                ExchangeTransactionEntity transaction = exchangeTransactionMapper.getExchangeTransactionByOrderId(orderId);
                messagePushFailService.sendMessage(transaction);
            }catch (Exception e){
                e.printStackTrace();
                log.error("兑换交易根据订单Id修改兑换订单状态推送消息失败!!");
            }
        }
        return ResponseData.ok();
    }



    /**
     * 后台-首页统计-基础数据看板-今日做兑换业务的笔数
     * @return
     */
    public ResponseData getTransactionCount(){
        JSONObject json = new JSONObject();
        TransactionParamerVO vo = new TransactionParamerVO();
        Long startTime = DatesUtil.getStartTime();
        Long endTime = DatesUtil.getEndTime();
        //开始时间和结束时间
        vo.setStartTime(startTime);
        vo.setEndTime(endTime);
        Integer count = exchangeTransactionMapper.getHistoryExchangeTransactionCount(vo);
        json.put("count",count);
        return ResponseData.ok(json);
    }


    /**
     * 后台-首页统计-基础数据看板-今日兑换得到的枚数
     * @return
     */
    public ResponseData getExchangeSymbolNumber(){
        List<ExchangeManagementVO> resultList = new ArrayList<>();
        Map<String ,Map<String , BigDecimal>> map = new HashMap<>();
        TransactionParamerVO vo = new TransactionParamerVO();
        Long startTime = DatesUtil.getStartTime();
        Long endTime = DatesUtil.getEndTime();
        //开始时间和结束时间
        vo.setStartTime(startTime);
        vo.setEndTime(endTime);
        vo.setPage(0);
        vo.setPageSize(100000);
        List<ExchangeTransactionEntity> list = exchangeTransactionMapper.getHistoryExchangeTransactionList(vo);
        for(ExchangeTransactionEntity entity : list){
            BigDecimal amount = entity.getToAmount() == null ? new BigDecimal("0") : entity.getToAmount();
            String symbol = entity.getToCurrency()== null ? "" : entity.getToCurrency();
            String net = entity.getToNetwork() == null ? "" : entity.getToNetwork();
            if(map.containsKey(net)){
                Map<String , BigDecimal> symbolMap = map.get(net);
                if(symbolMap.containsKey(symbol)){
                    //获取该主币下剩余价格
                    BigDecimal balance = symbolMap.get(symbol);
                    balance = balance.add(amount);
                    symbolMap.put(symbol,balance);
                }else{
                    //无该类型主币，则将币种和当前金额放入
                    symbolMap.put(symbol,amount);
                }
            }else{
                Map<String , BigDecimal> symbolMap = new HashMap<>();
                symbolMap.put(symbol,amount);
                map.put(net,symbolMap);
            }

        }
        map.forEach((key,value)->{
            Map<String , BigDecimal> symbolMap = value;
            String net = key;
            symbolMap.forEach((keys,values)->{
                ExchangeManagementVO transaction = new ExchangeManagementVO();
                String symbol = keys;
                BigDecimal amount = values;
                transaction.setNet(net);
                transaction.setSymbol(symbol);
                transaction.setAmount(amount);
                resultList.add(transaction);
            });
        });
        return ResponseData.ok(resultList);
    }



    /**
     * 后台-首页统计-基础数据看板-今日做兑换业务的会员总数据
     * @return
     */
    public ResponseData getToDayExchangeUser(){
        BigDecimal userCount = new BigDecimal("0");
        JSONObject jsonObject = new JSONObject();
        List<String> address = new ArrayList<>();
        TransactionParamerVO vo = new TransactionParamerVO();
        Long startTime = DatesUtil.getStartTime();
        Long endTime = DatesUtil.getEndTime();
        //开始时间和结束时间
        vo.setStartTime(startTime);
        vo.setEndTime(endTime);
        vo.setPage(0);
        vo.setPageSize(100000);
        List<ExchangeTransactionEntity> list = exchangeTransactionMapper.getHistoryExchangeTransactionList(vo);
        for(ExchangeTransactionEntity entity : list){
            //获取兑换得到的地址
            address.add(entity.getPayoutAddress());
        }
        List<String> distinctList = address.stream().distinct().collect(Collectors.toList());

        try {
            HttpEntity<List<String>> request = new HttpEntity<>(distinctList,null);
            Object result = restTemplate.postForObject(selectMemberByAddressUrl,request,Object.class);
            JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(result));
            userCount = json.get("data") == null ? new BigDecimal("0") : new BigDecimal(json.get("data").toString());
        }catch (Exception e){
            e.printStackTrace();
            log.error("根据地址查询会员数量执行失败"+ e +"=====distinctList====="+ distinctList.toString());
            return null;
        }
        jsonObject.put("userCount",userCount);

        return ResponseData.ok(jsonObject);

    }


}