package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.*;
import com.block.chain.mapper.*;
import com.block.chain.service.QuotationHourService;
import com.block.chain.utils.Constant;
import com.block.chain.utils.DatesUtil;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.IntervalVO;
import com.block.chain.vo.QuotationParamerVO;
import com.block.chain.vo.QuotationResultVO;
import com.block.chain.vo.QuotationVO;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service("quotationHourService")
public class QuotationHourServiceImpl extends ServiceImpl<QuotationHourMapper, QuotationHourEntity> implements QuotationHourService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CurrencyTypeMapper currencyTypeMapper;

    @Autowired
    public QuotationMapper quotationMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private QuotationHourMapper quotationHourMapper;

    @Value("${BLOCKCC.KlineUrl}")
    private String klineUrl;

    @Value("${BLOCKCC.ApiKey}")
    private String apiKey;

    @Autowired
    private CurrencyChainInfoMapper currencyChainInfoMapper;

    @Autowired
    private TransactionBtcInfoMapper transactionBtcInfoMapper;



    /**
     * 所有币种小时K线行情录入
     */
    @Async
    public void saveQuotationHour(){

        List<CurrencyTypeEntity> CurrencyList = currencyTypeMapper.getCurrencyList();
        //获取所有的币种信息，并按照币种符号去重
        List<CurrencyTypeEntity> list = CurrencyList.stream()
                .filter(distinctByKeys(CurrencyTypeEntity::getSymbol))
                .collect(Collectors.toList());

        try {
        for(CurrencyTypeEntity entity : list){
            for (int i=0; i< 240; i++){
                Calendar startCal = Calendar.getInstance();
                startCal.add(Calendar.MONTH,-i);
                startCal.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
                startCal.set(Calendar.SECOND, 0);
                startCal.set(Calendar.MINUTE, 0);
                startCal.set(Calendar.HOUR_OF_DAY, 0);
                startCal.set(Calendar.MILLISECOND, 0);
                Long startTime = startCal.getTimeInMillis();

                Calendar endCal = Calendar.getInstance();
                endCal.add(Calendar.MONTH,-i);
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.SECOND, 59);
                endCal.set(Calendar.MINUTE, 59);
                endCal.set(Calendar.HOUR_OF_DAY, 23);
                endCal.set(Calendar.MILLISECOND, 999);
                Long endTime = endCal.getTimeInMillis();

                JSONArray array = saveQuotation(entity,startTime,endTime);
                System.out.println("币种参数"+entity.getSymbol()+"开始时间="+startTime+"结束时间=="+endTime+"币种符号=="+entity.getSymbol());
//                if(array.size()<=0){
//                    break;
//                }

                Thread.sleep(1000);
//              String aa ="";
            }
        }
        }catch (Exception e){
            e.printStackTrace();
            log.error("所有币种小时K线行情录入失败=="+ e);
        }
        System.out.println("所有币种小时K线行情录入完毕！");

    }



    /**
     * 所有币种昨日小时K线行情录入
     */
    @Async
    public void saveYesterdayQuotationHour(){
        List<CurrencyTypeEntity> CurrencyList = currencyTypeMapper.getCurrencyList();
        //获取所有的币种信息，并按照币种符号去重
        List<CurrencyTypeEntity> list = CurrencyList.stream()
                .filter(distinctByKeys(CurrencyTypeEntity::getSymbol))
                .collect(Collectors.toList());

        try {
            for(CurrencyTypeEntity entity : list){
//                   Long startTime = DatesUtil.getYesterdayStartTime();
//                   Long endTime = DatesUtil.getYesterdayEndTime();
                     //获取上一个小时的开始时间和结束时间
                     Long startTime = DatesUtil.getLastHourStartTime();
                     Long endTime = DatesUtil.getLastHourEndTime();

                     JSONArray array = saveQuotation(entity,startTime,endTime);
//                     System.out.println("币种参数"+entity.getSymbol()+"开始时间="+startTime+"结束时间=="+endTime+"币种符号=="+entity.getSymbol());

                     Thread.sleep(1000);

            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("所有币种上一个小时K线行情录入失败=="+ e);
        }
        System.out.println("所有币种上一个小时K线行情录入完毕！");

    }


    public JSONArray saveQuotation(CurrencyTypeEntity entity,long start,long end){
        boolean isReqeust = true;
        //交易所顺序Gate.io,币安,OK,Coinbase,火币
        //交易所查询顺序gate-io,binance,okex,gdax,huobipro,bitmex
        String symbol = entity.getSymbol();
        String upperSymbol = symbol.toUpperCase();

        JSONArray data = new JSONArray();
        //当币种符号为USDT和USDX时不走交易所逻辑
        if(!(Constant.USDT.equals(symbol)||Constant.USDX.equals(symbol))){
            //gate-io 交易所处理逻辑
            if (isReqeust) {
                //获取交易对
                String desc = "gate-io_" + upperSymbol + "_USDT";
                data = this.getData(desc, start, end);
                int size = data.size();
                if (size > 0) {
                    isReqeust = false;
                }
            }
            //binance 交易所处理逻辑
            if (isReqeust) {
                //获取交易对
                String desc = "binance_" + upperSymbol + "_USDT";
                data = this.getData(desc, start, end);
                int size = data.size();
                if (size > 0) {
                    isReqeust = false;
                }
            }
            //huobipro 交易所处理逻辑
            if (isReqeust) {
                //获取交易对
                String desc = "huobipro_" + upperSymbol + "_USDT";
                data = this.getData(desc, start, end);
                int size = data.size();
                if (size > 0) {
                    isReqeust = false;
                }
            }
        }
            //行情数据处理、
            if (!isReqeust && data.size() > 0) {
                List<QuotationHourEntity> list = new ArrayList<>();
//                for (Object obj : data) {
                   for(int i=0; i<=0;i++){
                    Object obj = data.get(i);
                    JSONObject json = JSONObject.parseObject(obj.toString());

                    BigDecimal quotationTime = json.get("T") == null ? new BigDecimal(0)  : new BigDecimal(json.get("T").toString()); //时间戳
                    Long voQuotationTime =Long.valueOf(quotationTime.toString());
                    BigDecimal openingPrice = json.get("o")  == null ? new BigDecimal(0)  : new BigDecimal(json.get("o").toString()) ;//开盘价
                    BigDecimal closingPrice = json.get("c")  == null ? new BigDecimal(0)  : new BigDecimal(json.get("c").toString()) ;//收盘价
                    BigDecimal minimumPrice = json.get("l")  == null ? new BigDecimal(0)  : new BigDecimal(json.get("l").toString()) ;//最低价
                    BigDecimal highestPrice = json.get("h") == null ?  new BigDecimal(0)  : new BigDecimal(json.get("h").toString()) ;//最高价
                    BigDecimal transactionVolume = json.get("v") == null ? new BigDecimal(0)  :new BigDecimal(json.get("v").toString())  ;//交易量

                    QuotationHourEntity vo = new QuotationHourEntity();
                    vo.setQuotationTime(voQuotationTime/1000);
                    vo.setOpeningPrice(openingPrice);
                    vo.setClosingPrice(closingPrice);
                    vo.setMinimumPrice(minimumPrice);
                    vo.setHighestPrice(highestPrice);
                    vo.setTransactionVolume(transactionVolume);
                    vo.setSymbol(entity.getSymbol());
                    list.add(vo);
                }
                mybatisBatchInsert(list);
            }
            return data;
    }

    public long mybatisBatchInsert(List<QuotationHourEntity> dataList) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        long beginTime = System.currentTimeMillis();

        try {
            QuotationHourMapper insertMapper = session.getMapper(QuotationHourMapper.class);
            for (QuotationHourEntity data : dataList) {
                insertMapper.insertQuotationHour(data);
            }
            session.commit();
            session.clearCache();
        } catch (Exception e) {

            session.rollback();
        } finally {
            session.close();
        }

        return System.currentTimeMillis() - beginTime;
    }


    /**
     * 交易所处理逻辑公用方法
     * @return
     */
    public JSONArray getData(String desc, long start, long end){
        JSONArray data = new JSONArray();
        String url = "";
        try{
            url = klineUrl+desc+"&interval=1h"+"&start="+start+"&end="+end+"&api_key="+apiKey;
            Object result = restTemplate.getForObject(url,Object.class);
            data = JSONArray.parseArray(JSONObject.toJSONString(result));
        } catch (Exception e) {
//            log.error("所有币种小时K线行情录入请求执行失败URL="+url);
            return data;
        }
        return data;
    }

    /**
     * 去重公共方法
     * @param keyExtractors
     * @param <T>
     * @return
     */
    private static <T> Predicate<T> distinctByKeys(Function<? super T, ?>... keyExtractors) {
        final Map<List<?>, Boolean> seen = new ConcurrentHashMap<>();
        return t ->
        {
            final List<?> keys = Arrays.stream(keyExtractors)
                    .map(ke -> ke.apply(t))
                    .collect(Collectors.toList());

            return seen.putIfAbsent(keys, Boolean.TRUE) == null;
        };
    }


    /**
     * 补全交易记录中的coinPrice,coinValue字段
     * coinPrice
     * coinValue
     */
    @Async
    public void addCompletionTransaction(){
        List<CurrencyChainInfoEntity> chainList = currencyChainInfoMapper.getAllChainInfo();

        if(CollectionUtils.isNotEmpty(chainList)){
            for(CurrencyChainInfoEntity entity : chainList){
                if(StringUtils.isNotEmpty(entity.getNet())){
                    String net = entity.getNet().toLowerCase();
                    String tableName = "transaction_" + net + "_info";
                    List<TransactionBtcInfoEntity> transactionList =transactionBtcInfoMapper.getTransactionByName(tableName);
                    List<TransactionBtcInfoEntity> list = new ArrayList<>();
                    for(TransactionBtcInfoEntity info : transactionList){
                        BigDecimal coinPrice = info.getCoinPrice();
                        BigDecimal coinValue = info.getCoinValue();
                        if(coinPrice == null || coinValue == null){
                            TransactionBtcInfoEntity transaction = new TransactionBtcInfoEntity();
                            String symbol = info.getSymbol();
                            Long blockTime = info.getBlockTime();
                            //如果symbol和net相等，为主币，则只需要取主币价格，否则需要分别取主币和代币的价格
                            if(blockTime!=null&&symbol!=null&&info.getNet()!=null){
                                BigDecimal diff = info.getDiff() == null ? new BigDecimal("0") : info.getDiff();
                                if(symbol.equals(info.getNet())){
                                    BigDecimal symbolPrice = updateCoinPriceOrValue(blockTime*1000,symbol);
                                    coinValue = diff.multiply(symbolPrice);
                                    transaction.setId(info.getId());
                                    transaction.setCoinPrice(symbolPrice);
                                    transaction.setCoinValue(coinValue.abs());
                                    list.add(transaction);
                                }else{
                                    BigDecimal symbolPrice =new BigDecimal("0");
                                    BigDecimal netPrice = new BigDecimal("0");
                                    if(symbol.equals(Constant.USDT)||symbol.equals(Constant.USDX)){
                                        symbolPrice = new BigDecimal("1");
                                        netPrice = updateCoinPriceOrValue(blockTime*1000,info.getNet());
                                    }if("BSC".equals(info.getNet())){
                                        if("BNB".equals(symbol)){
                                            symbolPrice = updateCoinPriceOrValue(blockTime*1000,symbol);
//                                            netPrice = updateCoinPriceOrValue(blockTime*1000,symbol);
                                            netPrice = symbolPrice;
                                        }else if(symbol.equals(Constant.USDT)||symbol.equals(Constant.USDX)){
                                            symbolPrice = new BigDecimal("1");
                                            netPrice = updateCoinPriceOrValue(blockTime*1000,"BNB");
                                        }else{
                                            symbolPrice = updateCoinPriceOrValue(blockTime*1000,symbol);
                                            netPrice = updateCoinPriceOrValue(blockTime*1000,"BNB");
                                        }
                                    }else if("HECO".equals(info.getNet())){
                                    if("HT".equals(symbol)){
                                        symbolPrice = updateCoinPriceOrValue(blockTime*1000,symbol);
//                                        netPrice = updateCoinPriceOrValue(blockTime*1000,symbol);
                                        netPrice = symbolPrice;
                                    }else if(symbol.equals(Constant.USDT)||symbol.equals(Constant.USDX)){
                                        symbolPrice = new BigDecimal("1");
                                        netPrice = updateCoinPriceOrValue(blockTime*1000,"HT");
                                    }else{
                                        symbolPrice = updateCoinPriceOrValue(blockTime*1000,symbol);
                                        netPrice = updateCoinPriceOrValue(blockTime*1000,"HT");
                                    }
                                    }else if(!symbol.equals(Constant.USDT)&&!symbol.equals(Constant.USDX)){
                                        symbolPrice = updateCoinPriceOrValue(blockTime*1000,symbol);
                                        netPrice = updateCoinPriceOrValue(blockTime*1000,info.getNet());
                                    }
                                    coinValue = diff.multiply(symbolPrice);
                                    transaction.setId(info.getId());
                                    transaction.setCoinPrice(netPrice);
                                    transaction.setCoinValue(coinValue.abs());
                                    list.add(transaction);
                                }
                            }
                        }
                    }

                    mybatisBatchUpdate(tableName,list);

                }
            }
            System.out.println("补全交易记录中的coinPrice,coinValue字段已处理完毕！" );
        }
    }

    /**
     * 大批量修改coinPrice和coinValue公共方法
     * @param tableName
     * @param dataList
     * @return
     */
    public long mybatisBatchUpdate(String tableName , List<TransactionBtcInfoEntity> dataList) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        long beginTime = System.currentTimeMillis();
        try {
            TransactionBtcInfoMapper mapper = sqlSession.getMapper(TransactionBtcInfoMapper.class);
            for (TransactionBtcInfoEntity data : dataList) {
                mapper.updateData(tableName, data);
            }
            sqlSession.commit();
            sqlSession.clearCache();
        } catch (Exception e) {
            sqlSession.rollback();
        } finally {
            sqlSession.close();
        }
        return System.currentTimeMillis() - beginTime;
    }

    /**
     * 获取对应symbol最接近交易时间的行情
     * @param blockTime
     * @param symbol
     * @return
     */
    public BigDecimal updateCoinPriceOrValue(Long blockTime,String symbol){
        BigDecimal price = new BigDecimal("0");
        Date date = new Date(blockTime);
        Calendar calendarStartHour = Calendar.getInstance();
        /* HOUR_OF_DAY 指示一天中的小时 */
        calendarStartHour.setTime(date);
        calendarStartHour.add(Calendar.HOUR_OF_DAY, -1);
        Long startHourTime = calendarStartHour.getTimeInMillis()/1000;

        Calendar calendarEndHour = Calendar.getInstance();
        /* HOUR_OF_DAY 指示一天中的小时 */
        calendarEndHour.setTime(date);
        calendarEndHour.add(Calendar.HOUR_OF_DAY, 1);
        Long endHourTime = calendarEndHour.getTimeInMillis()/1000;
        List<QuotationHourEntity> quotation =quotationHourMapper.getHourPrice(startHourTime,endHourTime,symbol);
        if(CollectionUtils.isEmpty(quotation)){
            Calendar calendarStartDay = Calendar.getInstance();
            calendarStartDay.setTime(date);
            calendarStartDay.add(Calendar.DAY_OF_MONTH, -1);
            Long startDayTime = calendarStartDay.getTimeInMillis()/1000;


            Calendar calendarEndDay = Calendar.getInstance();
            calendarEndDay.setTime(date);
            calendarEndDay.add(Calendar.DAY_OF_MONTH, 1);
            Long endDayTime = calendarEndDay.getTimeInMillis()/1000;
            quotation =quotationHourMapper.getHourPrice(startDayTime,endDayTime,symbol);
        }
        if(CollectionUtils.isNotEmpty(quotation)){
            QuotationHourEntity entity = quotation.get(0);
            price = entity.getClosingPrice();
        }
        return price;
    }


    public ResponseData getQuotationList(List<QuotationParamerVO> list){
        List<QuotationResultVO> resultList = new ArrayList<>();
        resultList = quotationHourMapper.getNearestData(list);
        return ResponseData.ok(resultList);
    }




}