package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.*;
import com.block.chain.entity.mongo.CompanyReceiveVO;
import com.block.chain.entity.mongo.CurrencyReceiveVO;
import com.block.chain.entity.mongo.SymbolCurrencyVO;
import com.block.chain.entity.mongo.TRX;
import com.block.chain.mapper.*;
import com.block.chain.service.MongoDBService;
import com.block.chain.utils.*;
import com.block.chain.vo.*;
import com.block.chain.vo.management.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Wallet related service -
 */
@Service("mongoDBService")
@Slf4j
public class MongoDBServiceImpl implements MongoDBService {
    @Autowired
    private CurrencyTypeMapper currencyTypeMapper;

    @Autowired
    private  CurrencyChainInfoMapper currencyChainInfoMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    public QuotationMapper quotationMapper;

    @Autowired
    private TransactionBtcInfoMapper transactionBtcInfoMapper;

    @Autowired
    private CurrencyPriceMapper currencyPriceMapper;

    @Autowired
    private CompanyAddressMapper companyAddressMapper;

    @Autowired
    private CommonService commonService;

    @Autowired
    private WalletCurrencyTransactionMapper walletCurrencyTransactionMapper;

    @Autowired
    private ExchangeTransactionMapper ExchangeTransactionMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PrimaryAddressMapper primaryAddressMapper;

    public ResponseData getReceiveStatisticsList(){
        Long startTime  = ToolUtils.getStartTime()/1000;
        Long endTime = ToolUtils.getEndTime()/1000;
        //获取昨日的开始和结束时间，用来和今日接收笔数比较判断是上升还是下降
        Date yesterdayStart =  DatesUtil.getBeginDayOfYesterday();
        Date yesterdayEnd =  DatesUtil.getEndDayOfYesterDay();
        Long yesterdayStartTime = yesterdayStart.getTime()/1000;
        Long yesterdayEndTime = yesterdayEnd.getTime()/1000;
        Query query = new Query();
        //$gt:大于$lt:小于$gte:大于或等于$lte:小于或等于
        query.addCriteria(Criteria.where("markTime").gte(startTime).lte(endTime));
        List<CurrencyReceiveVO> list = this.mongoTemplate.find(query, CurrencyReceiveVO.class,Constant.currency_receive);
        Query yestQuery = new Query();
        //$gt:大于$lt:小于$gte:大于或等于$lte:小于或等于
        yestQuery.addCriteria(Criteria.where("markTime").gte(yesterdayStartTime).lte(yesterdayEndTime));
        List<CurrencyReceiveVO> yestList = this.mongoTemplate.find(yestQuery, CurrencyReceiveVO.class,Constant.currency_receive);
        if(CollectionUtils.isNotEmpty(yestList)&&CollectionUtils.isNotEmpty(list)){
            CurrencyReceiveVO vo = list.get(0);
            Integer   quantityToday= vo.getQuantityToday();
            Integer  yestQuantityToday = yestList.get(0).getQuantityToday();
            Integer trendType = getTrendType(quantityToday,yestQuantityToday);
            vo.setTrendType(trendType);

            Map<String,Map<String,Integer>> ysetMap = getYestMap(yestList.get(0).getSymbolCurrency());
            if(CollectionUtils.isNotEmpty(vo.getSymbolCurrency())){
                for(SymbolCurrencyVO currencyVO : vo.getSymbolCurrency()){
                    String net = currencyVO.getNet();
                    String symbol = currencyVO.getSymbol();
                    Integer quantToday = currencyVO.getQuantityToday();
                    currencyVO.setTrendType(0);//默认为0
                    if(ysetMap.containsKey(net)){
                        Map<String,Integer> symbolMap = ysetMap.get(net);
                        if(symbolMap.containsKey(symbol)){
                            Integer yestQuantToday = symbolMap.get(symbol);
                            Integer trend = getTrendType(quantToday,yestQuantToday);
                            currencyVO.setTrendType(trend);
                        }
                    }
                }
            }
        }
        return ResponseData.ok(list);
    }

    public Integer getTrendType(Integer quantityToday ,Integer yestQuantityToday){
        Integer trendType = 0;//默认相等
        //如果今日数量大于昨日，则上升 1，否则下降 2
        if(quantityToday > yestQuantityToday ){
            trendType = 1;
        }
        if(quantityToday < yestQuantityToday ){
            trendType = 2;
        }
        return trendType;
    }

    //统计昨日的今日接收数量
    public Map<String,Map<String,Integer>> getYestMap(List<SymbolCurrencyVO> yestList){
        Map<String,Map<String,Integer>> map = new HashMap<>();
        if(CollectionUtils.isNotEmpty(yestList)){
            for(SymbolCurrencyVO  vo : yestList){
                String net = vo.getNet();
                String symbol = vo.getSymbol();
                Integer quantityToday = vo.getQuantityToday() == null ? 0 : vo.getQuantityToday();
                //已存在该公链，则直接放入对应map
                if(map.containsKey(net)){
                    Map<String,Integer> symbolMap = map.get(net);
                    symbolMap.put(symbol,quantityToday);
                }else{
                    Map<String,Integer> symbolMap = new HashMap<>();
                    map.put(net , symbolMap);
                }
            }
        }
        return map;
    }



    /**
     * 后台管理系统-首页统计接收统计-财务收款总额，笔数数据查询 - 时间段（周，月）
     * @return
     */
    public ResponseData getCompanyReceiveStatistics(Integer type){
        List<ReceiveStatisticVO> receiveList =  new ArrayList<>();
        Long startTime = null;
        Long endTime = null;
        // 1：上周(国际算法：即上上周日 - 上周六) 3：上月 4：上一年
        if(type == 1){
            Date lastWeekStart  = DatesUtil.getBeginOfLastWeek();
            Date lastWeekEnd  = DatesUtil.getEndOfLastWeek();
            startTime = lastWeekStart.getTime()/1000;
            endTime = lastWeekEnd.getTime()/1000;
        }
        if(type == 2){
            Date lastMonthStart  = DatesUtil.getBeginDayOfLastMonth();
            Date lastMonthEnd  = DatesUtil.getEndDayOfLastMonth();
            startTime = lastMonthStart.getTime()/1000;
            endTime = lastMonthEnd.getTime()/1000;
        }
        Query query = new Query();
        //$gt:大于$lt:小于$gte:大于或等于$lte:小于或等于
        query.addCriteria(Criteria.where("markTime").gte(startTime).lte(endTime));
        List<CompanyReceiveVO> list = this.mongoTemplate.find(query, CompanyReceiveVO.class,Constant.company_receive);
        return ResponseData.ok(list);
    }


    /**
     * 后台管理系统-首页统计接收统计-钱包接收币笔数趋势图(币种和net查询)
     * @return
     */
    public ResponseData getReceiveStatistics(String net,String symbol){
        JSONArray array = new JSONArray();
        Date start  = DatesUtil.getBeginDayOfSevenYesterday();//过去七天的开始时间 - 包含今天（1206日该需求和产品确认）
        Date end  = DatesUtil.getDayEnd();//今天的结束时间- 包含今天（1206日该需求和产品确认）
        Long startTime = start.getTime()/1000;
        Long endTime = end.getTime()/1000;
        Query query = new Query();
        //$gt:大于$lt:小于$gte:大于或等于$lte:小于或等于
        query.addCriteria(Criteria.where("markTime").gte(startTime).lte(endTime));
        List<CurrencyReceiveVO> list = this.mongoTemplate.find(query, CurrencyReceiveVO.class,Constant.currency_receive);
        if(CollectionUtils.isNotEmpty(list)){
            //当公链和symbol不为空时，筛选对应的接收币笔数,为空时则拿总的筛选数据
            if(StringUtils.isNotEmpty(net)&&StringUtils.isNotEmpty(symbol)){
                for(CurrencyReceiveVO vo : list){
                    List<SymbolCurrencyVO> symbolCurrency = vo.getSymbolCurrency();
                    String createTime = vo.getCreateTime();
                    Long markTime = vo.getMarkTime();
                    for(SymbolCurrencyVO currencyVO : symbolCurrency){
                        if(symbol.equals(currencyVO.getSymbol())&&net.equals(currencyVO.getNet())){
                            JSONObject json = new JSONObject();
                            json.put("quantityHistorical",currencyVO.getQuantityToday());//当天接收笔数
                            json.put("net",net);
                            json.put("symbol",symbol);
                            json.put("createTime",createTime);
                            json.put("markTime",markTime);
                            array.add(json);
                        }
                    }
                }
            }
        }
        return ResponseData.ok(array);
    }





    /**
     * 后台管理系统-首页统计-发送统计数据查询 -当天数据
     * @return
     */
    public ResponseData getSendingStatisticsList(){
        Long startTime  = ToolUtils.getStartTime()/1000;
        Long endTime = ToolUtils.getEndTime()/1000;
        //获取昨日的开始和结束时间，用来和今日接收笔数比较判断是上升还是下降
        Date yesterdayStart =  DatesUtil.getBeginDayOfYesterday();
        Date yesterdayEnd =  DatesUtil.getEndDayOfYesterDay();
        Long yesterdayStartTime = yesterdayStart.getTime()/1000;
        Long yesterdayEndTime = yesterdayEnd.getTime()/1000;

        Query query = new Query();
        //$gt:大于$lt:小于$gte:大于或等于$lte:小于或等于
        query.addCriteria(Criteria.where("markTime").gte(startTime).lte(endTime));
        List<CurrencyReceiveVO> list = this.mongoTemplate.find(query, CurrencyReceiveVO.class,Constant.currency_sending);

        Query yestQuery = new Query();
        //昨日数据
        yestQuery.addCriteria(Criteria.where("markTime").gte(yesterdayStartTime).lte(yesterdayEndTime));
        List<CurrencyReceiveVO> yestList = this.mongoTemplate.find(yestQuery, CurrencyReceiveVO.class,Constant.currency_sending);
        if(CollectionUtils.isNotEmpty(yestList)&&CollectionUtils.isNotEmpty(list)){
            CurrencyReceiveVO vo = list.get(0);
            Integer   quantityToday= vo.getQuantityToday();
            Integer  yestQuantityToday = yestList.get(0).getQuantityToday();
            Integer trendType = getTrendType(quantityToday,yestQuantityToday);
            vo.setTrendType(trendType);

            Map<String,Map<String,Integer>> ysetMap = getYestMap(yestList.get(0).getSymbolCurrency());
            if(CollectionUtils.isNotEmpty(vo.getSymbolCurrency())){
                for(SymbolCurrencyVO currencyVO : vo.getSymbolCurrency()){
                    String net = currencyVO.getNet();
                    String symbol = currencyVO.getSymbol();
                    Integer quantToday = currencyVO.getQuantityToday();
                    currencyVO.setTrendType(0);//默认为0
                    if(ysetMap.containsKey(net)){
                        Map<String,Integer> symbolMap = ysetMap.get(net);
                        if(symbolMap.containsKey(symbol)){
                            Integer yestQuantToday = symbolMap.get(symbol);
                            Integer trend = getTrendType(quantToday,yestQuantToday);
                            currencyVO.setTrendType(trend);
                        }
                    }
                }
            }
        }
        return ResponseData.ok(list);
    }

    /**
     * 后台管理系统-首页统计发送统计-钱包发送币笔数趋势图(币种和net查询)
     * @return
     */
    public ResponseData getSendingStatistics(String net,String  symbol){
        JSONArray array = new JSONArray();
//        Date start  = DatesUtil.getBeginDayOfSevenYesterday();//过去七天的开始时间
//        Date end  = DatesUtil.getEndDayOfYesterDay();//昨天的结束时间
        Date start  = DatesUtil.getBeginDayOfSevenYesterday();//过去七天的开始时间 - 包含今天（1206日该需求和产品确认）
        Date end  = DatesUtil.getDayEnd();//今天的结束时间- 包含今天（1206日该需求和产品确认）
        Long startTime = start.getTime()/1000;
        Long endTime = end.getTime()/1000;
        Query query = new Query();
        //$gt:大于$lt:小于$gte:大于或等于$lte:小于或等于
        query.addCriteria(Criteria.where("markTime").gte(startTime).lte(endTime));
        List<CurrencyReceiveVO> list = this.mongoTemplate.find(query, CurrencyReceiveVO.class,Constant.currency_sending);
        if(CollectionUtils.isNotEmpty(list)){
            //当公链和symbol不为空时，筛选对应的接收币笔数,为空时则拿总的筛选数据
            if(StringUtils.isNotEmpty(net)&&StringUtils.isNotEmpty(symbol)){
                for(CurrencyReceiveVO vo : list){
                    List<SymbolCurrencyVO> symbolCurrency = vo.getSymbolCurrency();
                    String createTime = vo.getCreateTime();
                    Long markTime = vo.getMarkTime();
                    for(SymbolCurrencyVO currencyVO : symbolCurrency){
                        if(symbol.equals(currencyVO.getSymbol())&&net.equals(currencyVO.getNet())){
                            JSONObject json = new JSONObject();
                            json.put("quantityHistorical",currencyVO.getQuantityToday());//当天发送笔数
                            json.put("net",net);
                            json.put("symbol",symbol);
                            json.put("createTime",createTime);
                            json.put("markTime",markTime);
                            array.add(json);
                        }
                    }
                }
            }
        }

        return ResponseData.ok(array);
    }

    /**
     * 后台管理系统-首页统计-接收统计
     * @return
     */
    @Async
    public ResponseData currencyReceiveStatistics(){
        SimpleDateFormat simFormat = new SimpleDateFormat("yyyy-MM-dd");
        String createTime = simFormat.format(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Long markTime = cal.getTimeInMillis()/1000;
        CurrencyReceiveVO receive = new CurrencyReceiveVO();
        Integer quantityTodayTotal = 0;//今日接收数量 - 总
        Integer quantityHistoricalTotal = 0;//历史接收数量 - 总
        BigDecimal total = new BigDecimal(0);//总价值 - 总

        List<SymbolCurrencyVO> symbolCurrency = new ArrayList<>();
        JSONArray array = new JSONArray();
        List<CurrencyTypeEntity> currencyList = currencyTypeMapper.getCurrencyList();
        Long startTime = this.getStartTime();
        Long endTime = this.getEndTime();
        Map<String,BigDecimal> price =getPriceMap();//获取所有币种价格
        //所有公链下symbol对应的余额信息
        Map<String ,Map<String, BigDecimal>> balanceMap = getBalanceMap();
        for(CurrencyTypeEntity currency : currencyList){
            BigDecimal number = new BigDecimal(0);
            SymbolCurrencyVO vo = new SymbolCurrencyVO();
            String net = currency.getNet();
            String symbol = currency.getSymbol();
            String tableName = "transaction_" + net + "_info";
            tableName = tableName.toLowerCase();
            if(StringUtils.isNotEmpty(tableName)){
                Boolean isHave =commonService.isHaveTable(tableName);
                if(!isHave){
                    break;
                }
            }
            //历史总交易记录 - 根据公链和币种查询
            List<TransactionBtcInfoVO> historicalList =transactionBtcInfoMapper.getCurrencyReceiveStatistics(tableName,net,symbol,null,null,null);
            //今日总交易记录 - 根据公链和币种查询已经今日开始时间和结束时间
            List<TransactionBtcInfoVO> todayList =transactionBtcInfoMapper.getCurrencyReceiveStatistics(tableName,net,symbol,startTime,endTime,null);
            if(CollectionUtils.isNotEmpty(historicalList)){
                for(TransactionBtcInfoVO info : historicalList){
                    BigDecimal diff = info.getDiff() == null ? new BigDecimal(0) : info.getDiff();
                    number = number.add(diff);
                }
            }
            BigDecimal addressBalance = new BigDecimal(0);
            if(balanceMap.containsKey(net)){
                Map<String, BigDecimal> map = balanceMap.get(net);
                addressBalance = map.get(symbol) == null ? new BigDecimal(0) : map.get(symbol);
            }
            BigDecimal symbolPrice = price.get(symbol) == null ? new BigDecimal(0) :  price.get(symbol) ;
            BigDecimal totalValue = number.multiply(symbolPrice);
            vo.setNet(net);//公链
            vo.setSymbol(symbol);//币种
            vo.setHistoricalNumber(number);//历史接收币总和
            vo.setQuantityHistorical(historicalList.size());//历史交易数量
            vo.setQuantityToday(todayList.size());//今日交易数量
            vo.setTotalValue(totalValue);//接收总价值
            vo.setAddressBalance(addressBalance);//初始地址余额

            symbolCurrency.add(vo);
        }
        if(CollectionUtils.isNotEmpty(symbolCurrency)){
            for(SymbolCurrencyVO vo : symbolCurrency){
                Integer quantityToday = vo.getQuantityToday();
                Integer quantityHistorical = vo.getQuantityHistorical();
                BigDecimal totalValue = vo.getTotalValue();
                quantityTodayTotal = quantityTodayTotal + quantityToday;
                quantityHistoricalTotal = quantityHistoricalTotal + quantityHistorical;
                total = total.add(totalValue);
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String updateTime = format.format(new Date());
        receive.setQuantityToday(quantityTodayTotal);
        receive.setQuantityHistorical(quantityHistoricalTotal);
        receive.setTotalValue(total);
        receive.setSymbolCurrency(symbolCurrency);
        receive.setCreateTime(createTime);
        receive.setMarkTime(markTime);
        receive.setUpdateTime(updateTime);
        Query query = new Query();
        query.addCriteria(Criteria.where("createTime").is(createTime));
        List<CurrencyReceiveVO> trx = this.mongoTemplate.find(query, CurrencyReceiveVO.class,Constant.currency_receive);
        if(CollectionUtils.isNotEmpty(trx)){
            String id = trx.get(0).getId();
            Query monQuery = new Query();
            monQuery.addCriteria(Criteria.where("_id").is(id));
            Update update = new Update();
            update.set("quantityToday",receive.getQuantityToday());
            update.set("quantityHistorical",receive.getQuantityHistorical());
            update.set("totalValue",receive.getTotalValue());
            update.set("symbolCurrency",receive.getSymbolCurrency());
            update.set("updateTime",updateTime);
            this.mongoTemplate.upsert(monQuery,update,Constant.currency_receive);
            System.out.println("后台管理系统-首页统计 - 接收币统计 -修改" + format.format(new Date())+ Thread.currentThread().getName());
        }else{
            this.mongoTemplate.insert(receive,Constant.currency_receive);
            System.out.println("后台管理系统-首页统计 - 接收币统计 -新增" + format.format(new Date())+ Thread.currentThread().getName());

        }
        return ResponseData.ok("统计完成");
    }


    /**
     * 后台管理系统-首页统计-财务收款总额，笔数统计 - 主要统计公司地址相关数据
     * @return
     */
    @Async
    public ResponseData currencyCompanyReceiveStatistics(){

        SimpleDateFormat simFormat = new SimpleDateFormat("yyyy-MM-dd");
        String createTime = simFormat.format(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Long markTime = cal.getTimeInMillis()/1000;
        Integer quantityHistoricalTotal = 0;//历史接收数量 - 总
        BigDecimal total = new BigDecimal(0);//总价值 - 总
        //公司地址的历史接收总额 - 美元
        BigDecimal totalValue = new BigDecimal(0);
        Map<String,BigDecimal> price =getPriceMap();//获取所有币种价格
        List<CompanyAddressEntity> addressList=  companyAddressMapper.getAllCompanyAddress();
        Long startTime = this.getStartTime();
        Long endTime = this.getEndTime();
        //对地址进行分组，并且拿到公链下所有symbol对应的余额信息
        Map<String,List<String>> addressMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(addressList)){
            for(CompanyAddressEntity vo : addressList){
                String net = vo.getNet();
                String address = vo .getAddress();
                if(addressMap.containsKey(net)){
                    List<String> list = addressMap.get(net);
                    list.add(address);
                    addressMap.put(net,list);
                }else{
                    List<String> list = new ArrayList<>();
                    list.add(address);
                    addressMap.put(net,list);
                }
            }

        }
        //统计所有公司地址下的交易总额和交易笔数
        for (Map.Entry<String, List<String>> entry:addressMap.entrySet()) {
            String key = entry.getKey();
            List<String> value = entry.getValue();
            BigDecimal netNumber = new BigDecimal(0); //公司地址在每条公链下的价值之和
            Integer netQuantity = 0;//公司地址在每条公链下的交易记录之和
            String tableName = "transaction_" + key + "_info";
            tableName = tableName.toLowerCase();
            if (CollectionUtils.isNotEmpty(value)) {
                //对公链下所有公司地址去重
                List<String> valueList = value.stream().distinct().collect(Collectors.toList());
                for (String address : valueList) {
                    BigDecimal addressNumber = new BigDecimal(0);//地址下所有历史交易之和
                    //历史总交易记录 - 根据公链和币种查询
                    List<TransactionBtcInfoVO> historicalList = transactionBtcInfoMapper.getCurrencyReceiveStatistics(tableName, key, null, null, null, address);
                    List<TransactionBtcInfoVO> todayList = transactionBtcInfoMapper.getCurrencyReceiveStatistics(tableName, key, null, startTime, endTime, address);
                    if (CollectionUtils.isNotEmpty(historicalList)) {
                        int size = todayList.size();
                        //获取每个公司地址下的接收总额，以及接收交易记录数
                        for (TransactionBtcInfoVO info : historicalList) {
                            String symbol = info.getSymbol();
                            BigDecimal symbolPrice = price.get(symbol) == null ? new BigDecimal(0) : price.get(symbol);
                            BigDecimal diff = info.getDiff() == null ? new BigDecimal(0) : info.getDiff();
                            BigDecimal diffCost = diff.multiply(symbolPrice);//计算每一笔交易的价值$
                            addressNumber = addressNumber.add(diffCost);//计算该地址下所有交易的价值之和
                        }
                        netQuantity = netQuantity + size; //计算该公链下所有地址的当天接收笔数
                    }
                    netNumber = netNumber.add(addressNumber);//计算该公链下所有地址之和
                }
            }
            totalValue = totalValue.add(netNumber);//公司地址的历史接收总额
            quantityHistoricalTotal = quantityHistoricalTotal + netQuantity;//公司所有的地址当天的接收笔数
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String updateTime = format.format(new Date());
        CompanyReceiveVO receive = new CompanyReceiveVO();
        receive.setQuantityHistorical(quantityHistoricalTotal);//财务收款笔数
        receive.setTotalValue(totalValue);//财务收款总额
        receive.setUpdateTime(updateTime);
        receive.setCreateTime(createTime);
        receive.setMarkTime(markTime);//标识时间
        Query query = new Query();
        query.addCriteria(Criteria.where("createTime").is(createTime));
        List<CurrencyReceiveVO> trx = this.mongoTemplate.find(query, CurrencyReceiveVO.class,Constant.company_receive);
        if(CollectionUtils.isNotEmpty(trx)){
            String id = trx.get(0).getId();
            Query monQuery = new Query();
            monQuery.addCriteria(Criteria.where("_id").is(id));
            Update update = new Update();
            update.set("quantityHistorical",receive.getQuantityHistorical());
            update.set("totalValue",receive.getTotalValue());
            update.set("updateTime",updateTime);
            this.mongoTemplate.upsert(monQuery,update,Constant.company_receive);
            System.out.println("后台管理系统-首页统计 - 接收币统计 -修改" + format.format(new Date())+ Thread.currentThread().getName());
        }else{
            this.mongoTemplate.insert(receive,Constant.company_receive);
            System.out.println("后台管理系统-首页统计 - 接收币统计 -新增" + format.format(new Date())+ Thread.currentThread().getName());

        }
        return ResponseData.ok("统计完成");
    }



    /**
     * 后台管理系统-首页统计-发送统计
     * @return
     */
    @Async
    public ResponseData currencySendingStatistics(){
        SimpleDateFormat simFormat = new SimpleDateFormat("yyyy-MM-dd");
        String createTime = simFormat.format(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Long markTime = cal.getTimeInMillis()/1000;
        CurrencyReceiveVO receive = new CurrencyReceiveVO();
        Integer quantityTodayTotal = 0;//今日发送数量 - 总
        Integer quantityHistoricalTotal = 0;//历史发送数量 - 总
        BigDecimal total = new BigDecimal(0);//总价值 - 总

        List<SymbolCurrencyVO> symbolCurrency = new ArrayList<>();
        List<CurrencyTypeEntity> currencyList = currencyTypeMapper.getCurrencyList();
        Long startTime = this.getStartTime();
        Long endTime = this.getEndTime();
        Map<String,BigDecimal> price =getPriceMap();//获取所有币种价格
        //所有公链下symbol对应的余额信息
        Map<String ,Map<String, BigDecimal>> balanceMap = getBalanceMap();
        for(CurrencyTypeEntity currency : currencyList){
            BigDecimal number = new BigDecimal(0);
            SymbolCurrencyVO vo = new SymbolCurrencyVO();
            String net = currency.getNet();
            String symbol = currency.getSymbol();
            String tableName = "transaction_" + net + "_info";
            tableName = tableName.toLowerCase();
            if(StringUtils.isNotEmpty(tableName)){
                Boolean isHave =commonService.isHaveTable(tableName);
                if(!isHave){
                    break;
                }
            }
            //历史总交易记录 - 根据公链和币种查询
            List<TransactionBtcInfoVO> historicalList =transactionBtcInfoMapper.getCurrencySendingStatistics(tableName,net,symbol,null,null);
            //今日总交易记录 - 根据公链和币种查询已经今日开始时间和结束时间
            List<TransactionBtcInfoVO> todayList =transactionBtcInfoMapper.getCurrencySendingStatistics(tableName,net,symbol,startTime,endTime);
            if(CollectionUtils.isNotEmpty(historicalList)){
                for(TransactionBtcInfoVO info : historicalList){
                    //负数转正数
                    BigDecimal diff = info.getDiff() == null ? new BigDecimal(0) : info.getDiff().negate();
                    number = number.add(diff);
                }
            }
            BigDecimal addressBalance = new BigDecimal(0);
            if(balanceMap.containsKey(net)){
                Map<String, BigDecimal> map = balanceMap.get(net);
                addressBalance = map.get(symbol) == null ? new BigDecimal(0) : map.get(symbol);
            }
            BigDecimal symbolPrice = price.get(symbol) == null ? new BigDecimal(0) : price.get(symbol);
            BigDecimal totalValue = number.multiply(symbolPrice);
            vo.setNet(net);//公链
            vo.setSymbol(symbol);//币种
            vo.setHistoricalNumber(number);//历史接收币总和
            vo.setQuantityHistorical(historicalList.size());//历史交易数量
            vo.setQuantityToday(todayList.size());//今日交易数量
            vo.setTotalValue(totalValue);//接收总价值
            vo.setAddressBalance(addressBalance);//初始地址余额
            symbolCurrency.add(vo);
        }
        //如果公链下币种详细统计信息不为空，循环计算总数据统计
        if(CollectionUtils.isNotEmpty(symbolCurrency)){
            for(SymbolCurrencyVO vo : symbolCurrency){
                Integer quantityToday = vo.getQuantityToday();
                Integer quantityHistorical = vo.getQuantityHistorical();
                BigDecimal totalValue = vo.getTotalValue();
                quantityTodayTotal = quantityTodayTotal + quantityToday;
                quantityHistoricalTotal = quantityHistoricalTotal + quantityHistorical;
                total = total.add(totalValue);
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String updateTime = format.format(new Date());
        receive.setQuantityToday(quantityTodayTotal);
        receive.setQuantityHistorical(quantityHistoricalTotal);
        receive.setTotalValue(total);
        receive.setSymbolCurrency(symbolCurrency);
        receive.setCreateTime(createTime);
        receive.setMarkTime(markTime);
        receive.setUpdateTime(updateTime);
        Query query = new Query();
        query.addCriteria(Criteria.where("createTime").is(createTime));
        List<CurrencyReceiveVO> trx = this.mongoTemplate.find(query, CurrencyReceiveVO.class,Constant.currency_sending);
        if(CollectionUtils.isNotEmpty(trx)){
            String id = trx.get(0).getId();
            Query monQuery = new Query();
            monQuery.addCriteria(Criteria.where("_id").is(id));
            Update update = new Update();
            update.set("quantityToday",receive.getQuantityToday());
            update.set("quantityHistorical",receive.getQuantityHistorical());
            update.set("totalValue",receive.getTotalValue());
            update.set("symbolCurrency",receive.getSymbolCurrency());
            update.set("updateTime",updateTime);
            this.mongoTemplate.upsert(monQuery,update,Constant.currency_sending);
            System.out.println("后台管理系统-首页统计 - 发送币统计 -修改" +id+ format.format(new Date())+ Thread.currentThread().getName());
        }else{
            this.mongoTemplate.insert(receive,Constant.currency_sending);
            System.out.println("后台管理系统-首页统计 - 发送币统计 -新增" + format.format(new Date())+ Thread.currentThread().getName());

        }
        return ResponseData.ok("统计完成");
    }

    /**
     * 循环所有公链的余额表，拿到每条公链下symbol对应的余额信息
     * @return
     */
    public Map<String ,Map<String, BigDecimal>> getBalanceMap(){
        Map<String ,Map<String, BigDecimal>> map = new HashMap<>();
        //拿到所有的公链信息
        List<CurrencyChainInfoEntity> list = currencyChainInfoMapper.getAllChainInfo();
        if(CollectionUtils.isNotEmpty(list)){
            for(CurrencyChainInfoEntity chain : list){
                Map<String, BigDecimal> symbolMap = new HashMap<>();
                String net = chain.getNet();
                List<TRX> trxList = this.mongoTemplate.findAll(TRX.class,net);
                if(CollectionUtils.isNotEmpty(trxList)){
                    for(TRX trx : trxList){
                        String prmNet = trx.getNet();
                        BigDecimal prmBalance = trx.getBalance();
                        List<Map<String,Object>> tokens = trx.getTokens();
                        //主币symbol和余额放入map
                        if(symbolMap.containsKey(prmNet)){
                            BigDecimal balance = symbolMap.get(prmNet);
                            balance = balance.add(prmBalance);
                            symbolMap.put(prmNet,balance);
                        }else{
                            symbolMap.put(prmNet,prmBalance);
                        }
                        if(CollectionUtils.isNotEmpty(tokens)){
                            for (Map<String,Object> token : tokens){
                                String symbol = token.get("symbol") == null ? "": String.valueOf(token.get("symbol"));
                                BigDecimal tokenBalance = token.get("balance") == null ? new BigDecimal(0) :new BigDecimal(String.valueOf(token.get("balance")));
                                //匹配map中是否已存在该币种，如果存在则相加，不存在则新增
                                if(tokenBalance.compareTo(BigDecimal.ZERO)!=0){
                                    if(symbolMap.containsKey(symbol)){
                                        BigDecimal balance = symbolMap.get(symbol);
                                        balance = balance.add(tokenBalance);
                                        symbolMap.put(symbol,balance);
                                    }else{
                                        symbolMap.put(symbol,tokenBalance);
                                    }
                                }
                            }
                        }
                    }

                }
                map.put(net,symbolMap);
            }
        }
        return map;
    }


    //获取当天的开始时间
    public  Long getStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        todayStart.add(Calendar.HOUR_OF_DAY, -8);
        return todayStart.getTimeInMillis()/1000;
    }
    //获取当天的结束时间
    public  Long getEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MILLISECOND, 999);
        todayEnd.add(Calendar.HOUR_OF_DAY, -8);
        return todayEnd.getTimeInMillis()/1000;
    }

    /**
     * 获取所有币种对应价格
     * @return
     */
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


    /**
     * 会员管理 - 会员基础信息 - 加密数字资产管理
     * @return
     */
    public ResponseData getMemberAssets(List<TransactionAddressVO> addressList){
        List<MemberAssetVO> assetList = new ArrayList<>();
        //对地址进行分组，并且拿到公链下所有symbol对应的余额信息
        Map<String,List<String>>  addressMap = getNetAddressMap(addressList);
        Map<String ,Map<String, BigDecimal>> balanceMap = this.getAddressBalanceMap(addressMap);//获取所有地址和余额，按公链和symbol分组
        //会员信息 - 加密数字资产管理 - 历史兑换币数量获取
        Map<String ,Map<String, BigDecimal>> exchangeBalanceMap = this.getExchangeBalanceMap(addressMap);
        //后台管理系统 - 会员管理 - 会员基础信息 - 加密数字资产管理 - 按net，address，symbol统计入账信息
        Map<String ,Map<String, BigDecimal>> receiveNetMap = new HashMap<>();
        //后台管理系统 - 会员管理 - 会员基础信息 - 加密数字资产管理 - 按net，address，symbol统计出账信息
        Map<String ,Map<String, BigDecimal>> sendingNetMap = new HashMap<>();
        //拿到地址下所有币种的余额信息
        addressMap.forEach((key,value)->{
            Map<String, BigDecimal> receiveMap = new HashMap<>();
            Map<String, BigDecimal> sendingMap = new HashMap<>();
            String tableName = "transaction_" + key + "_info";
            tableName = tableName.toLowerCase();
            if(CollectionUtils.isNotEmpty(value)){
                for(String address : value){
                    List<MemberSymbolVO> receiveList  =transactionBtcInfoMapper.getReceiveGroupBySymbols(tableName, key, address);
                    if(CollectionUtils.isNotEmpty(receiveList)){
                        for(MemberSymbolVO vo : receiveList){
                            String symbol = vo.getSymbol();
                            BigDecimal number = vo.getDiff();
                            if(receiveMap.containsKey(symbol)){
                                BigDecimal count  = receiveMap.get(symbol);
                                count = count.add(number);
                                receiveMap.put(symbol,count);
                            }else{
                                receiveMap.put(symbol,number);
                            }
                        }
                    }
                    List<MemberSymbolVO> sendingList  =transactionBtcInfoMapper.getSendingGroupBySymbols(tableName, key, address);
                    if(CollectionUtils.isNotEmpty(sendingList)){
                        for(MemberSymbolVO vo :sendingList){
                            String symbol = vo.getSymbol();
                            BigDecimal number = vo.getDiff();
                            if(sendingMap.containsKey(symbol)){
                                BigDecimal count  = sendingMap.get(symbol);
                                count = count.add(number);
                                sendingMap.put(symbol,count);
                            }else{
                                sendingMap.put(symbol,number);
                            }
                        }
                    }
                }

            }

            receiveNetMap.put(key,receiveMap);
            sendingNetMap.put(key,sendingMap);
        });
//        addressMap.forEach((key,value)->{
//            Map<String, Integer> receiveMap = new HashMap<>();
//            Map<String, Integer> sendingMap = new HashMap<>();
//            String tableName = "transaction_" + key + "_info";
//            tableName = tableName.toLowerCase();
//            if(CollectionUtils.isNotEmpty(value)){
//                for(String address : value){
//                    List<MemberSymbolVO> receiveList  =transactionBtcInfoMapper.getReceiveGroupBySymbol(tableName, key, address);
//                    if(CollectionUtils.isNotEmpty(receiveList)){
//                        for(MemberSymbolVO vo : receiveList){
//                            String symbol = vo.getSymbol();
//                            Integer number = vo.getNumber();
//                            if(receiveMap.containsKey(symbol)){
//                                Integer count  = receiveMap.get(symbol);
//                                count = count + number;
//                                receiveMap.put(symbol,count);
//                            }else{
//                                receiveMap.put(symbol,number);
//                            }
//                        }
//                    }
//                    List<MemberSymbolVO> sendingList  =transactionBtcInfoMapper.getSendingGroupBySymbol(tableName, key, address);
//                    if(CollectionUtils.isNotEmpty(sendingList)){
//                        for(MemberSymbolVO vo :sendingList){
//                            String symbol = vo.getSymbol();
//                            Integer number = vo.getNumber();
//                            if(sendingMap.containsKey(symbol)){
//                                Integer count  = sendingMap.get(symbol);
//                                count = count + number;
//                                sendingMap.put(symbol,count);
//                            }else{
//                                sendingMap.put(symbol,number);
//                            }
//                        }
//                    }
//                }
//
//            }
//
//            receiveNetMap.put(key,receiveMap);
//            sendingNetMap.put(key,sendingMap);
//        });

        List<CurrencyTypeEntity> currencyList = currencyTypeMapper.getCurrencyList();
        for(CurrencyTypeEntity currency : currencyList){
            MemberAssetVO vo = new MemberAssetVO();
            String net = currency.getNet();
            String symbol = currency.getSymbol();
            String symbolBalance ="0";
            String exchangeBalance ="0";
            String receiveNumber ="0";
            String sendingNumber ="0";
            if(balanceMap.containsKey(net)){
                Map<String, BigDecimal> map = balanceMap.get(net);
                BigDecimal balance = map.get(symbol) == null ? new BigDecimal(0) : map.get(symbol);
                symbolBalance  = balance.toPlainString();
            }
            if(receiveNetMap.containsKey(net)){
                Map<String, BigDecimal> map = receiveNetMap.get(net);
                BigDecimal receiveNumbers = map.get(symbol) == null ? new BigDecimal(0) : map.get(symbol);
                receiveNumber = receiveNumbers.toPlainString();
            }
            if(sendingNetMap.containsKey(net)){
                Map<String, BigDecimal> map = sendingNetMap.get(net);
                BigDecimal sendingNumbers = map.get(symbol) == null ? new BigDecimal(0) : map.get(symbol);
                sendingNumber= sendingNumbers.negate().toPlainString();
            }
            if(exchangeBalanceMap.containsKey(net)){
                Map<String, BigDecimal> map = exchangeBalanceMap.get(net);
                BigDecimal balance = map.get(symbol) == null ? new BigDecimal(0) : map.get(symbol);
                exchangeBalance  = balance.toPlainString();
            }

            vo.setNet(net);//公链
            vo.setSymbol(symbol);//币种
            vo.setBalance(symbolBalance);//币种余额
            vo.setReceiveCount(receiveNumber);//历史接收
            vo.setSendingCount(sendingNumber);//历史发送
            vo.setExchangeCount(exchangeBalance);//历史兑换币数量
            vo.setAddress(addressMap.get(net));//钱包地址
            assetList.add(vo);
        }
        return ResponseData.ok(assetList);
    }


    /**
     * 对地址按公链进行分组
     * @param addressList
     * @return
     */
    public Map<String,List<String>>  getNetAddressMap(List<TransactionAddressVO> addressList){
        Map<String,List<String>> map = new HashMap<>();
        if(CollectionUtils.isNotEmpty(addressList)){
            for(TransactionAddressVO vo : addressList){
                String net = vo.getNet();
                String address = vo .getAddress();
                if(map.containsKey(net)){
                    List<String> list = map.get(net);
                    list.add(address);
                    map.put(net,list);
                }else{
                    List<String> list = new ArrayList<>();
                    list.add(address);
                    map.put(net,list);
                }
            }

        }
        return map;
    }


    /**
     * 对地址进行分组，并且拿到公链下所有symbol对应的余额信息
     * @param map 以分组地址信息
     * @return
     */
    public   Map<String ,Map<String, BigDecimal>>  getAddressBalanceMap(Map<String,List<String>> map){
        Map<String ,Map<String, BigDecimal>> balanceMap = new HashMap<>();//获取所有地址和余额，按公链和symbol分组
        //拿到地址下所有币种的余额信息
        map.forEach((key,value)->{
            //存放公链下所有币种的余额信息
            Map<String, BigDecimal> symbolMap = new HashMap<>();
            Query query = new Query();
            //$gt:大于$lt:小于$gte:大于或等于$lte:小于或等于
            query.addCriteria(Criteria.where("address").in(value));
            List<TRX> trxList = this.mongoTemplate.find(query,TRX.class,key);
            if(CollectionUtils.isNotEmpty(trxList)){
                for(TRX trx : trxList){
                    String prmNet = trx.getNet();
                    BigDecimal prmBalance = trx.getBalance();
                    List<Map<String,Object>> tokens = trx.getTokens();
                    //主币symbol和余额放入map
                    if(symbolMap.containsKey(prmNet)){
                        BigDecimal balance = symbolMap.get(prmNet);
                        balance = balance.add(prmBalance);
                        symbolMap.put(prmNet,balance);
                    }else{
                        symbolMap.put(prmNet,prmBalance);
                    }
                    if(CollectionUtils.isNotEmpty(tokens)){
                        for (Map<String,Object> token : tokens){
                            String symbol = token.get("symbol") == null ? "": String.valueOf(token.get("symbol"));
                            BigDecimal tokenBalance = token.get("balance") == null ? new BigDecimal(0) :new BigDecimal(String.valueOf(token.get("balance")));
                            //匹配map中是否已存在该币种，如果存在则相加，不存在则新增
                            if(tokenBalance.compareTo(BigDecimal.ZERO)!=0){
                                if(symbolMap.containsKey(symbol)){
                                    BigDecimal balance = symbolMap.get(symbol);
                                    balance = balance.add(tokenBalance);
                                    symbolMap.put(symbol,balance);
                                }else{
                                    symbolMap.put(symbol,tokenBalance);
                                }
                            }
                        }
                    }
                }
            }
            balanceMap.put(key,symbolMap);
        });
        return balanceMap;
     }

    /**
     * 后台管理系统 - 首页 - 加密数字版数据报表
     * @return
     */
    public ResponseData getYesterdayStatistics(){
        JSONObject json = new JSONObject();
        Date start  = DatesUtil.getBeginDayOfYesterday();//昨天的开始时间
        Date end  = DatesUtil.getEndDayOfYesterDay();//昨天的结束时间
        Long startTime = start.getTime()/1000;
        Long endTime = end.getTime()/1000;
        Query query = new Query();
        //$gt:大于$lt:小于$gte:大于或等于$lte:小于或等于
        query.addCriteria(Criteria.where("markTime").gte(startTime).lte(endTime));
        //获取昨日的接收币统计
        List<CurrencyReceiveVO> receiveList = this.mongoTemplate.find(query, CurrencyReceiveVO.class,Constant.currency_receive);
        //获取昨日的发送币统计
        List<CurrencyReceiveVO> sendingList = this.mongoTemplate.find(query, CurrencyReceiveVO.class,Constant.currency_sending);
        if(CollectionUtils.isNotEmpty(receiveList)&&CollectionUtils.isNotEmpty(sendingList)){
            CurrencyReceiveVO receive = receiveList.get(0);
            CurrencyReceiveVO sending = sendingList.get(0);
            json.put("receiveQuantity",receive.getQuantityHistorical());
            json.put("sendingQuantity",sending.getQuantityHistorical());
            json.put("receiveTotalValue",receive.getTotalValue());
            json.put("sendingTotalValue",sending.getTotalValue());
            json.put("createTime",receive.getCreateTime());
        }
        return ResponseData.ok(json);
    }


    /**
//     * 后台管理系统 - 首页统计 - 接收发送统计 - 历史接收币价值排名 - 当前用户总价值数据获取
     * 后台管理系统 - 会员管理 - 团队管理 - 团队虚拟资产统计数据获取
     * @param teamParamerMap 会员或团队名下对应的地址信息
     * @return
     */
    public ResponseData getTeamTotalAsset(Map<String , List<TransactionAddressVO>> teamParamerMap){
        Map<String, String> assetMap = new HashMap<>();
        Map<String, BigDecimal> price = commonService.getPriceMap();

        teamParamerMap.forEach((key,value)->{
            if(CollectionUtils.isNotEmpty(value)){
                //对会员用户下的地址按net进行分组
                Map<String,List<String>>  addressMap = getNetAddressMap(value);
                //拿到公链下所有symbol对应的余额信息
                Map<String ,Map<String, BigDecimal>> balanceMap =  getAddressBalanceMap(addressMap);
                //计算该用户名下所有数字货币资产总价值
                BigDecimal totalValue = getTotalValue(balanceMap,price);
                String asset = totalValue.stripTrailingZeros().toPlainString();
                assetMap.put(key,asset);
            }
        });
        return ResponseData.ok(assetMap);
    }


    /**
     * 后台管理系统 - 首页统计 - 接收发送统计 - 历史接收币价值排名 - 当前用户总价值数据和历史接收币总价值获取
     * teamParamerMap 会员或团队名下对应的地址信息
     * type 对应不同页面的逻辑 1 发送页面  2 接收页面 3 兑换页面
     * @return
     */
    public ResponseData getUserTotalReceieValue(HistoricalValueVO vo){
        Map<String , List<TransactionAddressVO>> teamParamerMap = vo.getTeamParamerMap();
        String type = vo.getType();
        Map<String, JSONObject> assetMap = new HashMap<>();
        Map<String, BigDecimal> price = commonService.getPriceMap();

        teamParamerMap.forEach((key,value)->{
            if(CollectionUtils.isNotEmpty(value)){
                JSONObject json = new JSONObject();
                //对会员用户下的地址按net进行分组
                Map<String,List<String>>  addressMap = getNetAddressMap(value);
                //拿到公链下所有symbol对应的余额信息
                Map<String ,Map<String, BigDecimal>> balanceMap =  getAddressBalanceMap(addressMap);
                //计算该用户名下所有数字货币资产总价值
                BigDecimal totalValue = getTotalValue(balanceMap,price);
                String asset = totalValue.stripTrailingZeros().toPlainString();
                //按类型获取对应历史交易总价值
                BigDecimal historyTotalValue  = getHistoryTotalValue(value,type,price);
                json.put("balanceValue",asset);
                json.put("historyTotalValue",historyTotalValue.stripTrailingZeros().toPlainString());
                assetMap.put(key,json);
            }
        });
        return ResponseData.ok(assetMap);
    }

    /**
     * 按照对应不同页面的逻辑 1 发送页面  2 接收页面 3 兑换页面计算最终的统计结果
     * @param list 会员对应的地址信息
     * @param type 不同的页面
     * @param price 币种对应的行情
     * @return
     */
    public BigDecimal getHistoryTotalValue(List<TransactionAddressVO> list , String type,Map<String, BigDecimal> price){
        BigDecimal historyTotalValue = new BigDecimal("0");
        List<String> addressList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(list)){
            for(TransactionAddressVO vo : list){
                String address  = vo.getAddress();
                addressList.add(address);
            }
            //type 对应不同页面的逻辑 1 发送页面  2 接收页面 3 兑换页面
            if(Constant.one.equals(type)){
                List<TransactionBtcInfoVO> transactions  = transactionBtcInfoMapper.getHistorySendingList(addressList);
                for(TransactionBtcInfoVO vo :transactions){
                    BigDecimal symbolPrice = new BigDecimal("0");
                    if(price.containsKey(vo.getSymbol())){
                        symbolPrice =  price.get(vo.getSymbol());
                    }
//                    BigDecimal symbolPrice = vo.getSymbol() == null ? new BigDecimal("0") : price.get(vo.getSymbol());
                    BigDecimal diff = vo.getDiff() == null ? new BigDecimal("0") : vo.getDiff().abs();
                    BigDecimal transaction =symbolPrice.multiply(diff);
                    historyTotalValue = historyTotalValue.add(transaction);
                }
            }else if(Constant.TWO.equals(type)){
                List<TransactionBtcInfoVO> transactions  = transactionBtcInfoMapper.getHistoryReceiveList(addressList);
                for(TransactionBtcInfoVO vo :transactions){
                    BigDecimal symbolPrice = new BigDecimal("0");
                    if(price.containsKey(vo.getSymbol())){
                        symbolPrice =  price.get(vo.getSymbol());
                    }
//                    BigDecimal symbolPrice = vo.getSymbol() == null ? new BigDecimal("0") : price.get(vo.getSymbol());
                    BigDecimal diff = vo.getDiff() == null ? new BigDecimal("0") : vo.getDiff().abs();
                    BigDecimal transaction =symbolPrice.multiply(diff);
                    historyTotalValue = historyTotalValue.add(transaction);
                }
            }else if(Constant.three.equals(type)){
                List<ExchangeTransactionEntity>  transactions= ExchangeTransactionMapper.getHistoryExchangeList(addressList);
                for(ExchangeTransactionEntity vo :transactions){
                    BigDecimal symbolPrice = new BigDecimal("0");
                    if(price.containsKey(vo.getToCurrency())){
                        symbolPrice =  price.get(vo.getToCurrency());
                    }
//                    BigDecimal symbolPrice = vo.getToCurrency() == null ? new BigDecimal("0") : price.get(vo.getToCurrency());
                    BigDecimal diff = vo.getToAmount() == null ? new BigDecimal("0") : vo.getToAmount().abs();
                    BigDecimal transaction =symbolPrice.multiply(diff);
                    historyTotalValue = historyTotalValue.add(transaction);
                }
            }
        }
        return historyTotalValue;
    }



    /**
     * 计算总价值
     * @param map 所有公链下对应币种余额信息
     * @param price 所有币种的价格信息
     * @return
     */
    public BigDecimal getTotalValue(Map<String ,Map<String, BigDecimal>> map,Map<String, BigDecimal> price){
        BigDecimal totalValue = new BigDecimal(0);
        //循环公链下所有币种余额，计算对应总价值
        for (Map.Entry<String ,Map<String, BigDecimal>>  entrys :map.entrySet()) {
            Map<String, BigDecimal> balanceMap  = entrys.getValue();
            //循环一条公链下所有的币种余额，计算对应总价值
            for (Map.Entry<String, BigDecimal> entry:balanceMap.entrySet()) {
                String symbol = entry.getKey();
                //币种对应余额
                BigDecimal symbolBalance = entry.getValue() == null ? new BigDecimal(0) : entry.getValue();
                //币种对应价格
                BigDecimal symbolPrice = price.get(symbol) == null ? new BigDecimal(0) : price.get(symbol);
                totalValue = totalValue.add(symbolBalance.multiply(symbolPrice));//该币种对应总价值
            }
        }
        return  totalValue;
    }


    /**
     * 后台管理系统 - 会员管理 - 会员资金管理 - 会员名下所有公链 symbol 数字货币余额获取
     * @return
     */
    public ResponseData getMemberAssetManagement(Map<String , List<TransactionAddressVO>> teamParamerMap){
        Map<String ,List<MemberAssetVO>> map = new HashMap<>();
        teamParamerMap.forEach((key,value)->{
            //对会员用户下的地址按net进行分组
            Map<String,List<String>>  addressMap = getNetAddressMap(value);
            //拿到公链下所有symbol对应的余额信息
            Map<String ,Map<String, BigDecimal>> balanceMap =  getAddressBalanceMap(addressMap);
            List<MemberAssetVO> assetList = getMemberSymbolBalance(balanceMap);
            map.put(key , assetList);
        });
        return ResponseData.ok(map);
    }


    /**
     * 获取会员名下所有币种的余额信息
     * @param balanceMap
     * @return
     */
    public List<MemberAssetVO> getMemberSymbolBalance(Map<String ,Map<String, BigDecimal>> balanceMap){
        List<MemberAssetVO> list = new ArrayList<>();
        List<CurrencyTypeEntity> currencyList = currencyTypeMapper.getCurrencyList();
        for(CurrencyTypeEntity entity : currencyList){
            MemberAssetVO vo = new MemberAssetVO();
            String net = entity.getNet();
            String symbol = entity.getSymbol();
            vo.setNet(net);
            vo.setSymbol(symbol);
            vo.setBalance("0");
            if(balanceMap.containsKey(net)){
                Map<String, BigDecimal> symbolMap = balanceMap.get(net);
                if(symbolMap.containsKey(symbol)){
                    BigDecimal balance = symbolMap.get(symbol) == null ? new BigDecimal(0) : symbolMap.get(symbol);
                    vo.setBalance(balance.stripTrailingZeros().toPlainString());
                }
            }
            list.add(vo);
        }
        return list;
    }


    /**
     * 批量获取获取公链地址下余额数据 - 地址余额记录入MongoDB库后
     * @param addressList 查询地址信息
     * @return
     */
    public ResponseData getMongoAddressBalanceList(List<TransactionAddressVO> addressList){
        List<AddressBalanceListVO> list = new ArrayList<>();
        Map<String,List<String>>  addressMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(addressList)){
            addressMap = getNetAddressMap(addressList);
        }
        addressMap.forEach((key,value)-> {
            Query query = new Query();
            //$gt:大于$lt:小于$gte:大于或等于$lte:小于或等于
            query.addCriteria(Criteria.where("address").in(value));
            List<TRX> trxList = this.mongoTemplate.find(query,TRX.class,key);
            if(CollectionUtils.isNotEmpty(trxList)) {
                for (TRX trx : trxList) {
                    AddressBalanceListVO vo = new AddressBalanceListVO();
                    List<BalanceTokensVO> tokenList = new ArrayList<>();
                    String address = trx.getAddress() == null ? "": trx.getAddress();
                    String net = trx.getNet() == null ? "": trx.getNet();

                    BigDecimal primaryBalance = trx.getBalance();
                    List<Map<String,Object>> tokens = trx.getTokens();
                    if(CollectionUtils.isNotEmpty(tokens)){
                        for(Map<String , Object> map : tokens){
                            BalanceTokensVO token = new BalanceTokensVO();
                            String symbol = String.valueOf(map.get("symbol")) == null ? "": String.valueOf(map.get("symbol"));
                            String addressName = String.valueOf(map.get("address")) == null ? "": String.valueOf(map.get("address"));
//                            Integer decimal = (Integer) map.get("decimal");
                            Integer decimal = map.get("decimal") == null ? 0 : Integer.valueOf(map.get("decimal").toString());
                            BigDecimal balance = String.valueOf(map.get("balance")) == null ? new BigDecimal(0) :new BigDecimal(String.valueOf(map.get("balance")));
                            String balanceString = this.bigDecimalConvert(balance);
                            Integer coinType = (Integer) map.get("coinType");
                            token.setSymbol(symbol);
                            token.setAddress(addressName);
                            token.setNet(net);
                            token.setBalance(balanceString);
                            token.setDecimal(decimal);
                            token.setCoinType(coinType);
                            tokenList.add(token);
                        }
                    }
                    String primaryBalanceString = bigDecimalConvert(primaryBalance);
                    vo.setAddress(address);
                    vo.setNet(net);
                    vo.setBalance(primaryBalanceString);
                    vo.setTokens(tokenList);
                    list.add(vo);
                }
            }
        });

        return ResponseData.ok(list);
    }


    /**
     * 处理Bigdecimal数据，解决科学计数法，以及末尾多余零的问题  - 公共方法
     * @param parameter 需要处理的Bigdecimal参数
     * @return
     */
    public String bigDecimalConvert(Object parameter){
        String decimal = "0";
        if(parameter != null){
            decimal = parameter == null ? "0":new BigDecimal(parameter.toString()).stripTrailingZeros().toPlainString();
        }
        return decimal;
    }


    /**
     * 会员信息 - 加密数字资产管理 - 历史兑换币数量获取
     * @param map 以分组地址信息
     * @return
     */
    public Map<String ,Map<String, BigDecimal>>  getExchangeBalanceMap(Map<String,List<String>> map){
        Map<String ,Map<String, BigDecimal>> balanceMap = new HashMap<>();//获取所有地址和余额，按公链和symbol分组
        //拿到地址下所有币种的兑换金额信息
        map.forEach((key,value)->{
            //存放公链下所有币种的兑换金额信息
            Map<String, BigDecimal> symbolMap = new HashMap<>();
            if(CollectionUtils.isNotEmpty(value)){
                TransactionParamerVO vo = new TransactionParamerVO();
                vo.setAddress(value);
                List<WalletCurrencyTransactionEntity> list = walletCurrencyTransactionMapper.getExchangeList(value,key);
                for(WalletCurrencyTransactionEntity entity : list){
//                    //扣除币相关信息
//                    String entryNet = entity.getEntryNet();
//                    String entrySymbol = entity.getEntrySymbol();
//                    BigDecimal entryAmount = entity.getEntryAmount();
//                    //扣除币 - 币数量整理
//                    if(symbolMap.containsKey(entrySymbol)){
//                        BigDecimal balance = symbolMap.get(entrySymbol);
//                        balance = balance.add(entryAmount);
//                        symbolMap.put(entrySymbol,balance);
//                    }else{
//                        symbolMap.put(entrySymbol,entryAmount);
//                    }

                    //得到币相关信息 - 和田总沟通- 兑换只统计得到币 2023-002-07
                    String outNet = entity.getOutNet();
                    String outSymbol = entity.getOutSymbol();
                    BigDecimal outAmount = entity.getOutAmount();
                    //得到币 - 币数量整理
                    if(symbolMap.containsKey(outSymbol)){
                        BigDecimal balance = symbolMap.get(outSymbol);
                        balance = balance.add(outAmount);
                        symbolMap.put(outSymbol,balance);
                    }else{
                        symbolMap.put(outSymbol,outAmount);
                    }
                }
            }
            balanceMap.put(key,symbolMap);
        });
        return balanceMap;
    }





    public ResponseData getAddressByNet(String net, Integer page, Integer pageSize){
        List<PrimaryAddressEntity> list = primaryAddressMapper.getAddressList(net,(page-1)*pageSize,pageSize);
        Integer listCount = primaryAddressMapper.getAddressCount(net);
        PageUtils pageUtils = new PageUtils(list,listCount,pageSize,page);
        return ResponseData.ok(pageUtils);
    }



    public ResponseData getBalanceByAddress(BalanceManagementVO manageVO){
        List<AddressBalanceListVO> resultList = new ArrayList<>();
        String key = manageVO.getNet();
        List<String> value = manageVO.getAddress();
        Query query = new Query();
        //$gt:大于$lt:小于$gte:大于或等于$lte:小于或等于
        query.addCriteria(Criteria.where("address").in(value));
        List<TRX> trxList = this.mongoTemplate.find(query,TRX.class,key);
        if(CollectionUtils.isNotEmpty(trxList)) {
            for (TRX trx : trxList) {
                AddressBalanceListVO vo = new AddressBalanceListVO();
                List<BalanceTokensVO> tokenList = new ArrayList<>();
                String address = trx.getAddress() == null ? "": trx.getAddress();
                String net = trx.getNet() == null ? "": trx.getNet();

                BigDecimal primaryBalance = trx.getBalance();
                List<Map<String,Object>> tokens = trx.getTokens();
                if(CollectionUtils.isNotEmpty(tokens)){
                    for(Map<String , Object> map : tokens){
                        BalanceTokensVO token = new BalanceTokensVO();
                        String symbol = String.valueOf(map.get("symbol")) == null ? "": String.valueOf(map.get("symbol"));
                        String addressName = String.valueOf(map.get("address")) == null ? "": String.valueOf(map.get("address"));
//                            Integer decimal = (Integer) map.get("decimal");
                        Integer decimal = map.get("decimal") == null ? 0 : Integer.valueOf(map.get("decimal").toString());
                        BigDecimal balance = String.valueOf(map.get("balance")) == null ? new BigDecimal(0) :new BigDecimal(String.valueOf(map.get("balance")));
                        String balanceString = this.bigDecimalConvert(balance);
                        Integer coinType = (Integer) map.get("coinType");
                        token.setSymbol(symbol);
                        token.setAddress(addressName);
                        token.setNet(net);
                        token.setBalance(balanceString);
                        token.setDecimal(decimal);
                        token.setCoinType(coinType);
                        tokenList.add(token);
                    }
                }
                String primaryBalanceString = bigDecimalConvert(primaryBalance);
                vo.setAddress(address);
                vo.setNet(net);
                vo.setBalance(primaryBalanceString);
                vo.setTokens(tokenList);
                resultList.add(vo);
            }
        }
        return ResponseData.ok(resultList);
    }



}