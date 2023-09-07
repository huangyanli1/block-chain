package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.entity.CurrencyTypeEntity;
import com.block.chain.entity.QuotationEntity;
import com.block.chain.mapper.CurrencyPriceMapper;
import com.block.chain.mapper.CurrencyTypeMapper;
import com.block.chain.mapper.QuotationMapper;
import com.block.chain.service.QuotationService;
import com.block.chain.utils.Constant;
import com.block.chain.utils.R;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.IntervalVO;
import com.block.chain.vo.QuotationVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.client.RestTemplate;



@Service("quotationService")
public class QuotationServiceImpl extends ServiceImpl<QuotationMapper, QuotationEntity> implements QuotationService {

    @Autowired
    public QuotationMapper quotationMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CurrencyTypeMapper currencyTypeMapper;

    @Autowired
    private CurrencyPriceMapper currencyPriceMapper;

    /**
     * 按条件筛选历史行情数据
     * @param symbol 币种符号
     * @param intervalType K线间隔
     * @param startTime 开始时间(时间戳)
     * @param endTime 结束时间(时间戳)
     * @return
     */
    public ResponseData getQuotationList(String symbol,String intervalType,String startTime ,String endTime){
        String symbols = symbol.toLowerCase();
        List<QuotationEntity> quotationList = new ArrayList<>();
        IntervalVO vo = new IntervalVO();
        //前后固定获取表名
        String tableName = "quotation_" + symbols + "_usd";
        vo.setTableName(tableName);
        vo.setIntervalType(intervalType);
        vo.setStartTime(startTime);
        vo.setEndTime(endTime);
        if(StringUtils.isNotEmpty(intervalType)){
            if(StringUtils.isNotEmpty(startTime)&&StringUtils.isNotEmpty(endTime)){
                quotationList = quotationMapper.getQuotationList(vo);
            }else{
                quotationList = quotationMapper.getQuotationSizeList(vo);
            }
        }
        return ResponseData.ok(quotationList);
    }





    /**
     * 自动建表
     * @param tableName 表名
     * @return
     */
    public R automaticCreateTable(String tableName){
        int size  = quotationMapper.automaticCreateTable(tableName);
        if(size <= 0){
            return R.error(500,"自动建表失败！");
        }
        return R.ok("自动建表成功");
    }

    /**
     * 获取币种价格
     * @param urls 查询URL
     * @return
     */
    public R getCurrencyPrice(String urls){
        List<CurrencyTypeEntity> CurrencyList = currencyTypeMapper.getCurrencyList();
        //获取所有的币种信息，并按照币种符号去重
        List<CurrencyTypeEntity> list = CurrencyList.stream()
                .filter(distinctByKeys(CurrencyTypeEntity::getSymbol))
                .collect(Collectors.toList());
        boolean isReqeust = true;

        String symbols = "";
//        for(CurrencyTypeEntity entity : list){
//            String symbol = entity.getSymbol();
//            symbols = symbols+","+symbol;
//        }
        //用来处理特殊symbol，如USDT和USDX
        List<String> usdtList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++){
            String symbol = list.get(i).getSymbol();
            if(Constant.USDX.equals(symbol)||Constant.USDT.equals(symbol)){
                usdtList.add(symbol);
            }
            if(i == 0){
                symbols = symbols + symbol;
            }else{
                symbols = symbols+","+symbol;
            }

        }
        JSONArray data = new JSONArray();
        if(StringUtils.isNotEmpty(symbols)){
            data = this.getData(symbols);
            if(data.size() > 0){
                for (Object obj : data) {
                    QuotationVO vo = new QuotationVO();
                    JSONObject json = JSONObject.parseObject(obj.toString());
                    Object S = json.get("S"); //symbol
                    CurrencyPriceEntity saveEntity = this.getPrice(json);
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
            if(CollectionUtils.isNotEmpty(usdtList)){
                Calendar cal = Calendar.getInstance();
                String  timeEnd= String.valueOf(cal.getTimeInMillis()/1000)+"000";
                long time = Long.valueOf(timeEnd);
                this.updateSpecialSymbol(usdtList,time);
            }
        }
        return R.ok("OK");
    }

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
     * 交易所处理逻辑公用方法
     * @return
     */
    public JSONArray getData(String symbol){
        JSONArray data = new JSONArray();
        String url = "";
        try{
            url = "https://data.mifengcha.com/api/v3/price?symbol="+symbol+"&api_key=OJXLQXX1VO7PMCUJHVQXMHTW23PXUXRTGJGEMA93";
            Object result = restTemplate.getForObject(url,Object.class);
            data = JSONArray.parseArray(JSONObject.toJSONString(result));
        } catch (Exception e) {
            log.error("15秒实时获取币种价格请求执行失败URL="+url);
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
     * 判断数据库中是否存在某一张表
     * @param tableName
     * @return
     */
    public R getIsHaveTable(String tableName){
        int  size = quotationMapper.isHaveTable(tableName);
        return R.ok();
    }

}