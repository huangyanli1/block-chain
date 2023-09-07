package com.block.chain.scheduled;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyTypeEntity;
import com.block.chain.mapper.CurrencyTypeMapper;
import com.block.chain.mapper.QuotationMapper;
import com.block.chain.utils.Constant;
import com.block.chain.vo.QuotationVO;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 行情相关定时任务 - 1dK线
 */
@Slf4j
@Component
public class QuotationOneDaySchedule {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CurrencyTypeMapper currencyTypeMapper;

    @Autowired
    public QuotationMapper quotationMapper;

    @Value("${BLOCKCC.KlineUrl}")
    private String klineUrl;


    @Value("${BLOCKCC.ApiKey}")
    private String apiKey;


//    @Scheduled(cron = "0 0 08 */1 * ?")
    @SchedulerLock(name = "1dK线", lockAtMostFor = Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void visitCountTaskByOneDay(){
        //获取间隔的开始时间和结束时间
        //举例，1分钟间隔内，开始时间和结束时间间隔59秒，否则会查出来两条数据,
        //踩坑事件：蜜蜂查用的是秒级+000的毫秒，如果直接获取时间的毫秒级当参数去查询会导致查询不到数据
        Calendar cal = Calendar.getInstance();
        //北京时间转为格林威治时间
        cal.add(Calendar.HOUR_OF_DAY, -8);
        long time = cal.getTimeInMillis();
        cal.add(cal.DATE,-1);
        String timeStart = String.valueOf(cal.getTimeInMillis()/1000)+"000";
        long start = Long.valueOf(timeStart);
        cal.add(cal.DATE,1);
        String  timeEnd= String.valueOf(cal.getTimeInMillis()/1000)+"000";
        long end = Long.valueOf(timeEnd);
//        System.out.println("1d====现在开始时间是=" + start + "结束时间是="+end);

        List<CurrencyTypeEntity> CurrencyList = currencyTypeMapper.getCurrencyList();
        //获取所有的币种信息，并按照币种符号去重
        List<CurrencyTypeEntity> list = CurrencyList.stream()
                .filter(distinctByKeys(CurrencyTypeEntity::getSymbol))
                .collect(Collectors.toList());

        //交易所来源
        String exchange = "";

        for(CurrencyTypeEntity entity : list) {
            boolean isReqeust = true;
            //交易所顺序Gate.io,币安,OK,Coinbase,火币
            //交易所查询顺序gate-io,binance,okex,gdax,huobipro,bitmex
            String symbol = entity.getSymbol();
            String upperSymbol = symbol.toUpperCase();
            String symbols = symbol.toLowerCase();
            //前后固定获取表名
            String tableName = "quotation_" + symbols + "_usd";
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
                        exchange = "gate-io";
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
                        exchange = "binance";
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
                        exchange = "huobipro";
                    }
                }
            }
            int isHaveTable = quotationMapper.isHaveTable(tableName);
            //判断数据库中是否存在该行情表，当存在行情表时才会进行数据录入操作
            if (isHaveTable > 0) {
                //行情数据处理、
                if (!isReqeust && data.size() > 0) {
                    for (Object obj : data) {
                        QuotationVO vo = new QuotationVO();
                        JSONObject json = JSONObject.parseObject(obj.toString());

                        Object quotationTime = json.get("T"); //时间戳
                        Long voQuotationTime =Long.valueOf(quotationTime.toString());
                        //蜜蜂查在59秒内会拿到两条数据，分别这分钟和下一分钟的，所以要把下一分钟的去掉
                        if(voQuotationTime >= start&&voQuotationTime<=end){
                            Object openingPrice = json.get("o");//开盘价
                            Object closingPrice = json.get("c");//收盘价
                            Object minimumPrice = json.get("l");//最低价
                            Object highestPrice = json.get("h");//最高价
                            Object transactionVolume = json.get("v");//交易量
                            vo.setTableName(tableName);
                            vo.setQuotationTime(voQuotationTime);
                            vo.setOpeningPrice(new BigDecimal(openingPrice.toString()));
                            vo.setClosingPrice(new BigDecimal(closingPrice.toString()));
                            vo.setMinimumPrice(new BigDecimal(minimumPrice.toString()));
                            vo.setHighestPrice(new BigDecimal(highestPrice.toString()));
                            vo.setTransactionVolume(new BigDecimal(transactionVolume.toString()));
                            vo.setIntervalType("1d");
                            vo.setExchange(exchange);
                            int id = quotationMapper.insertQuotation(vo);
                        }
                    }
                }else{
                    QuotationVO vo = new QuotationVO();
                    if(Constant.USDT.equals(symbol)||Constant.USDX.equals(symbol)){
                        BigDecimal price = new BigDecimal(1);
                        BigDecimal volume = new BigDecimal(0);
                        vo.setTableName(tableName);
                        vo.setQuotationTime(start);
                        vo.setOpeningPrice(price);
                        vo.setClosingPrice(price);
                        vo.setMinimumPrice(price);
                        vo.setHighestPrice(price);
                        vo.setTransactionVolume(volume);
                        vo.setIntervalType("1d");
                    }else{
                        BigDecimal price = new BigDecimal(0);
                        vo.setTableName(tableName);
                        vo.setQuotationTime(start);
                        vo.setOpeningPrice(price);
                        vo.setClosingPrice(price);
                        vo.setMinimumPrice(price);
                        vo.setHighestPrice(price);
                        vo.setTransactionVolume(price);
                        vo.setIntervalType("1d");
                    }
                    int id = quotationMapper.insertQuotation(vo);
                }
            }
        }
    }


    /**
     * 交易所处理逻辑公用方法
     * @return
     */
    public JSONArray getData(String desc,long start,long end){
        JSONArray data = new JSONArray();
        String url ="";
        try{
            url = klineUrl+desc+"&interval=1d"+"&start="+start+"&end="+end+"&api_key="+apiKey;
            Object result = restTemplate.getForObject(url,Object.class);
            data = JSONArray.parseArray(JSONObject.toJSONString(result));
        } catch (Exception e) {
            log.error("1d请求执行失败URL="+url);
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



}
