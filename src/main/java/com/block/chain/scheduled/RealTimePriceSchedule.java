package com.block.chain.scheduled;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.entity.CurrencyTypeEntity;
import com.block.chain.mapper.CurrencyPriceMapper;
import com.block.chain.mapper.CurrencyTypeMapper;
import com.block.chain.mapper.QuotationMapper;
import com.block.chain.service.MessagePushFailService;
import com.block.chain.utils.Constant;
import com.block.chain.vo.QuotationVO;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 实时获取价格定时任务 - 15秒线
 */
@Slf4j
@Component
public class RealTimePriceSchedule {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CurrencyTypeMapper currencyTypeMapper;

    @Autowired
    public QuotationMapper quotationMapper;

    @Autowired
    private CurrencyPriceMapper currencyPriceMapper;

    @Autowired
    private MessagePushFailService messagePushFailService;

    @Value("${BLOCKCC.PriceUrl}")
    private String priceUrl;

    @Value("${BLOCKCC.ApiKey}")
    private String apiKey;

    @Value("${EXTERNA.QuotationUrl}")
    private String quotationUrl;




//    @Scheduled(cron = "0/15 * * * * ?")
    @SchedulerLock(name = "币种价格15秒", lockAtMostFor =Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void visitCountTaskByRealTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println("币种价格15秒=现在时间是" + format.format(new Date())+ Thread.currentThread().getName());
        List<CurrencyTypeEntity> CurrencyList = currencyTypeMapper.getCurrencyList();
        //获取所有的币种信息，并按照币种符号去重
        List<CurrencyTypeEntity> list = CurrencyList.stream()
                .filter(distinctByKeys(CurrencyTypeEntity::getSymbol))
                .collect(Collectors.toList());
        boolean isReqeust = true;
        String symbols = "";
        List<String> usdtList = new ArrayList<>();
        List<String> symbolsList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String symbol = list.get(i).getSymbol();
            if (!"WOW".equals(symbol)) {
                if (Constant.USDX.equals(symbol) || Constant.USDT.equals(symbol)) {
                    usdtList.add(symbol);
                }
                if (i % 50 == 0) {
                    symbolsList.add(symbols);
                    symbols = symbol;
                } else {
                    symbols = symbols + "," + symbol;
                }
            }
        }
        symbolsList.add(symbols);

        for(String symbol : symbolsList){
            if(StringUtils.isNotEmpty(symbol)){
                logicalProcessing(symbol);
            }
        }
    }

    public void logicalProcessing(String symbols){
        JSONArray data = new JSONArray();
        if(StringUtils.isNotEmpty(symbols)){
            data = this.getData(symbols);
            if(data.size() > 0){
                for (Object obj : data) {
                    QuotationVO vo = new QuotationVO();
                    JSONObject json = JSONObject.parseObject(obj.toString());
                    Object S = json.get("S"); //symbol
                    CurrencyPriceEntity saveEntity = this.getPrice(json);
//                    if(Constant.ETH.equals(S.toString())){
//                        CurrencyPriceEntity fEthEntity = new CurrencyPriceEntity();
//                        BeanUtils.copyProperties(saveEntity, fEthEntity);
//                        fEthEntity.setCurrencySymbol(Constant.FETH);
//                        fEthEntity.setCurrencyName(Constant.FETH_Name);
//                        this.saveFETHPrice(fEthEntity,Constant.FETH);
//                    }
                    List<CurrencyPriceEntity> priceList = currencyPriceMapper.getCurrencyPrice(S.toString());
                    //币种价格表如果有symbol，则修改，否则新增
                    if(CollectionUtils.isNotEmpty(priceList)){
                        CurrencyPriceEntity entity = priceList.get(0);
                        Long id = entity.getId();
                        saveEntity.setId(id);
                        currencyPriceMapper.updateById(saveEntity);
                    }else{
                        currencyPriceMapper.insert(saveEntity);
                    }
                }
            }
            //此逻辑用于处理特殊币种，后期会使用
//            if(CollectionUtils.isNotEmpty(usdtList)){
//                Calendar cal = Calendar.getInstance();
//                String  timeEnd= String.valueOf(cal.getTimeInMillis()/1000)+"000";
//                long time = Long.valueOf(timeEnd);
//                this.updateSpecialSymbol(usdtList,time);
//            }
        }
        List<CurrencyPriceEntity> priceList = currencyPriceMapper.getALLCurrencyPrice();
        if(CollectionUtils.isNotEmpty(priceList)){
            messagePushFailService.sendCurrencyPriceMessage(priceList);
        }
    }




    /**
     * 交易所处理逻辑公用方法
     * @return
     */
    public JSONArray getData(String symbol){
        JSONArray data = new JSONArray();
        String url = "";
        try{
            url = priceUrl+symbol+"&page=1&size=100&api_key="+apiKey;
            Object result = restTemplate.getForObject(url,Object.class);
            data = JSONArray.parseArray(JSONObject.toJSONString(result));
        } catch (Exception e) {
            log.error("15秒实时获取币种价格请求执行失败URL="+url);
            return data;
        }
        return data;
    }

    /**
     * 生成合格币种价格实体类
     * @param json
     * @return
     */
    public CurrencyPriceEntity getPrice(JSONObject json){
        CurrencyPriceEntity entity = new CurrencyPriceEntity();
        if(json != null){
            String s = json.get("s") == null ? null : json.get("s").toString();//币种名称
            String S = json.get("S") == null ? null : json.get("S").toString();//币种符号
            Long T = Long.valueOf(json.get("T").toString());//时间戳(毫秒)
            BigDecimal u = json.get("u") == null ? null : new BigDecimal(json.get("u").toString());//价格(USD)
            BigDecimal b = json.get("b") == null ? null : new BigDecimal(json.get("b").toString());//价格(BTC)
            BigDecimal a = json.get("a") == null ? null : new BigDecimal(json.get("a").toString());//交易量(单位为当前币种)
            BigDecimal v = json.get("v") == null ? null : new BigDecimal(json.get("v").toString());//交易量(USD)
            BigDecimal ra =json.get("ra")== null ? null : new BigDecimal(json.get("ra").toString());//报告交易量(单位为当前币种)
            BigDecimal rv =json.get("rv")== null ? null : new BigDecimal(json.get("rv").toString());//报告交易量(USD)
            BigDecimal m = json.get("m") == null ? null : new BigDecimal(json.get("m").toString());//市值(USD)
            BigDecimal c = json.get("c") == null ? null : new BigDecimal(json.get("c").toString());//24小时涨跌幅
            BigDecimal h = json.get("h") == null ? null : new BigDecimal(json.get("h").toString());//24小时最高价
            BigDecimal l = json.get("l") == null ? null : new BigDecimal(json.get("l").toString());//24小时最低价
            BigDecimal cw = json.get("cw") == null ? null : new BigDecimal(json.get("cw").toString());//1周涨跌幅
            BigDecimal hw = json.get("hw") == null ? null : new BigDecimal(json.get("hw").toString());//1周最高价
            BigDecimal lw = json.get("lw") == null ? null : new BigDecimal(json.get("lw").toString());//1周最低价
            BigDecimal cm = json.get("cm") == null ? null : new BigDecimal(json.get("cm").toString());//1月涨跌幅
            BigDecimal hm = json.get("hm") == null ? null : new BigDecimal(json.get("hm").toString());//1月最高价
            BigDecimal lm = json.get("lm") == null ? null : new BigDecimal(json.get("lm").toString());//1月最低价
            BigDecimal ha = json.get("ha") == null ? null : new BigDecimal(json.get("ha").toString());//历史最高价
            BigDecimal la = json.get("la") == null ? null : new BigDecimal(json.get("la").toString());//历史最低价
            entity.setCurrencyName(s);
            entity.setCurrencySymbol(S);
            entity.setPriceUsd(u);
            entity.setTransactionVolume(v);
            entity.setTimeMs(T);
            entity.setTransactionCurrency(a);
            entity.setTransactionReport(ra);
            entity.setTransactionReportUsd(rv);
            entity.setMarketValue(m);
            entity.setRiseFallDay(c);
            entity.setHighestPriceDay(h);
            entity.setMinimumPriceDay(l);
            entity.setRiseFallWeek(cw);
            entity.setHighestPriceWeek(hw);
            entity.setMinimumPriceWeek(lw);
            entity.setRiseFallMonth(cm);
            entity.setHighestPriceMonth(hm);
            entity.setMinimumPriceMonth(lm);
            entity.setHighestPriceHistory(ha);
            entity.setMinimumPriceHistory(la);
        }

        return entity;
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
     * 特殊币种处理 - 暂不处理
     * @param list
     * @param timeMs
     * @return
     */
    public boolean updateSpecialSymbol(List<String> list,long timeMs){
        boolean isUpdate = true ;
        if(CollectionUtils.isNotEmpty(list)){
            for(String symbol : list){
                List<CurrencyPriceEntity> priceList = currencyPriceMapper.getCurrencyPrice(symbol);
                //币种价格表如果有symbol，则修改，否则新增
                if(CollectionUtils.isNotEmpty(priceList)){
                    CurrencyPriceEntity saveEntity = new CurrencyPriceEntity();
                    CurrencyPriceEntity entity = priceList.get(0);
                    Long id = entity.getId();
                    saveEntity.setId(id);
                    saveEntity.setTimeMs(timeMs);
                    currencyPriceMapper.updateById(saveEntity);
                    isUpdate = true;
                }else{
                    CurrencyPriceEntity entity = new CurrencyPriceEntity();
                    if(Constant.USDT.equals(symbol)){
                        entity.setCurrencyName("Tether");
                        entity.setCurrencySymbol("USDT");
                    }
                    if(Constant.USDX.equals(symbol)){
                        entity.setCurrencyName("USDX");
                        entity.setCurrencySymbol("USDX");
                    }
                    BigDecimal price =new BigDecimal(1);
                    BigDecimal volume =new BigDecimal(0);
                    entity.setPriceUsd(price);
                    entity.setTransactionVolume(volume);
                    entity.setTimeMs(timeMs);
                    entity.setTransactionCurrency(volume);
                    entity.setTransactionReport(volume);
                    entity.setTransactionReportUsd(volume);
                    entity.setMarketValue(price);
                    entity.setRiseFallDay(price);
                    entity.setHighestPriceDay(price);
                    entity.setMinimumPriceDay(price);
                    entity.setRiseFallWeek(price);
                    entity.setHighestPriceWeek(price);
                    entity.setMinimumPriceWeek(price);
                    entity.setRiseFallMonth(price);
                    entity.setHighestPriceMonth(price);
                    entity.setMinimumPriceMonth(price);
                    entity.setHighestPriceHistory(price);
                    entity.setMinimumPriceHistory(price);
                    currencyPriceMapper.insert(entity);
                    isUpdate = true;
                }

            }
        }
        return isUpdate;
    }


    public void getArbUsdtPrice(){

        List<CurrencyPriceEntity> priceList = currencyPriceMapper.getCurrencyPrice(Constant.ARBI_SYMBOL);
        //币种价格表如果有symbol，则修改，否则新增
        BigDecimal arbPrice = getArbPrice();
        if(CollectionUtils.isNotEmpty(priceList)){
            CurrencyPriceEntity entity = priceList.get(0);
            Long id = entity.getId();
            entity.setPriceUsd(arbPrice);
            currencyPriceMapper.updateById(entity);
        }else{
            CurrencyPriceEntity saveEntity = new CurrencyPriceEntity();
            saveEntity.setPriceUsd(arbPrice);
            saveEntity.setCurrencySymbol(Constant.ARBI_SYMBOL);
            saveEntity.setCurrencyName(Constant.ARBI_NAME);
            currencyPriceMapper.insert(saveEntity);
        }
    }


    /**
     * 交易所处理逻辑公用方法
     * @return
     */
    public BigDecimal getArbPrice(){
        String url = "";
        BigDecimal c = new BigDecimal("0");
        try{
            Object result = restTemplate.getForObject(quotationUrl,Object.class);
            JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(result));
            c = json.get("c") == null ? new BigDecimal("0") : new BigDecimal(json.get("c").toString());//价格(USD)
        } catch (Exception e) {
            log.error("需要特殊处理的币种价格请求执行失败URL="+url);
            return c;
        }
        return c;
    }

    public void saveFETHPrice(CurrencyPriceEntity saveEntity,String S){
        List<CurrencyPriceEntity> priceList = currencyPriceMapper.getCurrencyPrice(S);
        //币种价格表如果有symbol，则修改，否则新增
        if(CollectionUtils.isNotEmpty(priceList)){
            CurrencyPriceEntity entity = priceList.get(0);
            Long id = entity.getId();
            saveEntity.setId(id);
            currencyPriceMapper.updateById(saveEntity);
        }else{
            currencyPriceMapper.insert(saveEntity);
        }
    }



}
