package com.block.chain.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.*;
import com.block.chain.mapper.*;
import com.block.chain.service.AvailablePairsService;
import com.block.chain.service.MessagePushFailService;
import com.block.chain.utils.Constant;
import com.block.chain.utils.RandomGUID;
import com.block.chain.utils.ResponseData;
import com.block.chain.utils.SHA256Utils;
import com.block.chain.utils.redis.RedisKeyName;
import com.block.chain.utils.redis.RedisUtil;
import com.block.chain.vo.convert.ExchangeRestltVO;
import com.block.chain.vo.convert.ExchangeTransactionParamerVO;
import com.block.chain.vo.convert.ExchangeTransactionRestltVO;
import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("availablePairsService")
public class AvailablePairsServiceImpl extends ServiceImpl<AvailablePairsMapper, AvailablePairsEntity> implements AvailablePairsService {

    @Autowired
    private CurrencyChainInfoMapper currencyChainInfoMapper;

    @Autowired
    private CurrencyTypeMapper currencyTypeMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AvailablePairsMapper availablePairsMapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private AvailableCurrenciesMapper availableCurrenciesMapper;

    @Autowired
    private ExchangeTransactionMapper exchangeTransactionMapper;

    @Autowired
    private MessagePushFailService messagePushFailService;

    @Value("${EXCHANGENOW.AVAILABLEPAIRSURL}")
    private String AvailablePairsUrl;

    @Value("${EXCHANGENOW.XCHANGENOWAPIKEY}")
    private String ApiKey;

    @Value("${EXCHANGENOW.AVAILABLECURRENCIES}")
    private String AvailableCurrenciesUrl;

    @Value("${EXCHANGENOW.MINAMOUNTURL}")
    private String MINAMOUNTURL;

    @Value("${EXCHANGENOW.ESTIMATEDAMOUNTURL}")
    private String ESTIMATEDAMOUNTURL;

    @Value("${EXCHANGENOW.EXCHANGEURL}")
    private String EXCHANGEURL;

    @Value("${EXCHANGENOW.TRANSACTIONSTATUSURL}")
    private String TRANSACTIONSTATUSURL;

    /**
     *  Exchange Now 可用币对录入接口
     * @return
     */
    public ResponseData saveAvailablePairs(){
        //存储所有从exchange-now获取的可用币对
        List<AvailablePairsEntity> list = new ArrayList<>();
        Map<String,List<String>> symbolMap = new HashMap<>();
        //获取所有的公链信息
        List<CurrencyChainInfoEntity> chainList = currencyChainInfoMapper.getAllChainInfo();
        //获取所有的币种信息
        List<CurrencyTypeEntity> currencyTypeList = currencyTypeMapper.getCurrencyList();
        for(CurrencyTypeEntity entity : currencyTypeList){
            String net = entity.getNet().toLowerCase();
            String symbol = entity.getSymbol().toLowerCase();
            if(symbolMap.containsKey(net)){
                List<String> symbolList = symbolMap.get(net);
                symbolList.add(symbol);
                symbolMap.put(net,symbolList);
            }else{
                List<String> symbolList = new ArrayList<>();
                symbolList.add(symbol);
                symbolMap.put(net,symbolList);
            }
        }

        //按net对币种信息进行分组
        Map<String, List<CurrencyTypeEntity>>  currencyTypeMap = currencyTypeList.stream().collect(
                Collectors.groupingBy(CurrencyTypeEntity::getNet));
        if(CollectionUtils.isNotEmpty(chainList)){
            //为了避免频繁调用调用导致请求被exchange now锁死，每次只调用链 - 链的可用币对，如，ETH - TRX下所有的可用币对
            for(CurrencyChainInfoEntity entity : chainList){
              String fromNet = entity.getNet().toLowerCase();
//                String fromNet = "eth";

                //剔除exchange now不支持的heco链
                if(!Constant.notsupport_network.equals(fromNet)){
                    for(CurrencyChainInfoEntity chain : chainList){
                      String toNet = chain.getNet().toLowerCase();
                        if(!Constant.notsupport_network.equals(toNet)){
                            String availablePairsUrl = AvailablePairsUrl+"?fromCurrency=&toCurrency=&fromNetwork="+fromNet.toLowerCase()+"&toNetwork="+toNet.toLowerCase()+"&flow=";
                            JSONArray array = getExchangeNowResponse(availablePairsUrl);
                            List<String> fromSymbol = symbolMap.get(fromNet);
                            List<String> toSymbol = symbolMap.get(toNet);
                            for(Object obj : array){
                                JSONObject tokenJson = JSONObject.parseObject(obj.toString());
                                String fromCurrency = tokenJson.get("fromCurrency") == null ? "":tokenJson.get("fromCurrency").toString();
                                String toCurrency = tokenJson.get("toCurrency") == null ? "":tokenJson.get("toCurrency").toString();
                                if(fromSymbol.contains(fromCurrency)&&toSymbol.contains(toCurrency)){
                                    AvailablePairsEntity pair = new AvailablePairsEntity();
                                    String fromNetwork = tokenJson.get("fromNetwork") == null? "":tokenJson.get("fromNetwork").toString();
                                    String toNetwork = tokenJson.get("toNetwork") == null? "":tokenJson.get("toNetwork").toString();
                                    String flow = tokenJson.get("flow") == null? "":tokenJson.get("flow").toString();
                                    pair.setFromCurrency(fromCurrency.toUpperCase());
                                    pair.setFromNetwork(fromNetwork.toUpperCase());
                                    pair.setToCurrency(toCurrency.toUpperCase());
                                    pair.setToNetwork(toNetwork.toUpperCase());
                                    pair.setFlow(flow);
                                    list.add(pair);
                                }
                            }
                        }
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(list)){
                availablePairsMapper.remove();
                mybatisBatchInsert(list);
            }

        }
        return ResponseData.ok("可用币对插入成功");
    }

    public long mybatisBatchInsert(List<AvailablePairsEntity> dataList) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        long beginTime = System.currentTimeMillis();

        try {
            AvailablePairsMapper insertMapper = session.getMapper(AvailablePairsMapper.class);
            for (AvailablePairsEntity data : dataList) {
                insertMapper.insertAvailablePairs(data);
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
     * 调用exchange now接口的公用方法
     * @return
     */
    public JSONArray getExchangeNowResponse(String url){
        JSONArray data = new JSONArray();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-changenow-api-key", ApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);
            String  jsonStr = JSON.parse(response.getBody()).toString();
            data= JSONObject.parseArray(jsonStr);
        } catch (Exception e) {
            return data;
        }
        return data;
    }


    /**
     * 币种对应的exchange now可用币对插入redis缓存
     * @return
     */
    public ResponseData getSymbolAvailablePairs(){
        Map<String ,List<AvailablePairsEntity>> map = new HashMap<>();

        //获取所有的币种信息
        List<CurrencyTypeEntity> currencyTypeList = currencyTypeMapper.getCurrencyList();
        try{
        if(CollectionUtils.isNotEmpty(currencyTypeList)){
            for(CurrencyTypeEntity entity : currencyTypeList){
                String net = entity.getNet();
                String symbol = entity.getSymbol();
                String redisKey = Constant.available_pairs+"-"+net+"-"+symbol;
                List<AvailablePairsEntity> list = availablePairsMapper.getAvailablePairsList(net,symbol);
                redisUtil.set(redisKey, JSONObject.toJSONString(list), Long.valueOf(0));
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("币种对应的可用币对插入redis缓存报错=="+ e.getMessage());
        }
        return ResponseData.ok("币种对应的可用币对插入redis缓存成功");
    }


    /**
     * 币种对应的exchange now可用币对插入redis缓存  - 反向兑换币种
     * @return
     */
    public ResponseData getSymbolAvailablePairsOppositeDirection(){
        Map<String ,List<AvailablePairsEntity>> map = new HashMap<>();

        //获取所有的币种信息
        List<CurrencyTypeEntity> currencyTypeList = currencyTypeMapper.getCurrencyList();
        try{
            if(CollectionUtils.isNotEmpty(currencyTypeList)){
                for(CurrencyTypeEntity entity : currencyTypeList){
                    String net = entity.getNet();
                    String symbol = entity.getSymbol();
                    String redisKey = Constant.available_pairs_opposite_direction+"-"+net+"-"+symbol;
                    List<AvailablePairsEntity> list = availablePairsMapper.getAvailablePairsList(net,symbol);
                    redisUtil.set(redisKey, JSONObject.toJSONString(list), Long.valueOf(0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("币种对应的可用币对插入redis缓存报错=="+ e.getMessage());
        }
        return ResponseData.ok("币种对应的可用币对插入redis缓存成功");
    }


    /**
     * 通过公链net和symbol获取对应可兑换币对
     * @param net
     * @param symbol
     * @return
     */
    public ResponseData getRedisAvailablePairs(String net ,String symbol){
        JSONArray resultArray = new JSONArray();
        Map<String,JSONObject> map = new HashMap<>();
        if(StringUtils.isEmpty(net)||StringUtils.isEmpty(symbol)){
            return ResponseData.fail("查询参数错误！");
        }
        //获取所有的币种信息
        List<CurrencyTypeEntity> currencyTypeList = currencyTypeMapper.getCurrencyList();
        for(CurrencyTypeEntity entity : currencyTypeList){
            String currencySymbol = entity.getSymbol() == null? "":entity.getSymbol();
            String fullName = entity.getFullName() == null? "":entity.getFullName();
            String logoUrl = entity.getLogoUrl() == null? "":entity.getLogoUrl();
            if(!map.containsKey(currencySymbol)){
                JSONObject json = new JSONObject();
                json.put("fullName",fullName);
                json.put("logoUrl",logoUrl);
                map.put(currencySymbol,json);
            }
        }
//        // 去除重复的数据
//        ArrayList<CurrencyTypeEntity> collect = currencyTypeList.stream().collect(Collectors.collectingAndThen(
//                Collectors.toCollection(() -> new TreeSet<>(
//                        Comparator.comparing(
//                                // 对象的某一个属性
//                                CurrencyTypeEntity::getSymbol))), ArrayList::new));

        String redisKey = Constant.available_pairs+"-"+net+"-"+symbol;
        String availablePairs = redisUtil.getString(redisKey);
        if(StringUtils.isNotEmpty(availablePairs)){
            JSONArray jsonArray = JSONArray.parseArray(availablePairs);
            for(Object object :jsonArray){
                JSONObject tokenJson = JSONObject.parseObject(object.toString());
                JSONObject json = new JSONObject();
                String toCurrency = tokenJson.get("toCurrency") == null ? "":tokenJson.get("toCurrency").toString();
                String toNetwork = tokenJson.get("toNetwork") == null ? "":tokenJson.get("toNetwork").toString();
                json.put("symbol",toCurrency);
                json.put("net",toNetwork);
                JSONObject jsonObject = map.get(toCurrency);
                if(jsonObject != null){
                    json.put("fullName",jsonObject.get("fullName"));
                    json.put("logoUrl",jsonObject.get("logoUrl"));
                }
                resultArray.add(json);
            }
        }
        return ResponseData.ok(resultArray);
    }


    /**
     * 拉取exchange- now可用币种并入库
     * @return
     */
    public ResponseData saveAvailableCurrencies(){
        //获取所有的公链信息
        List<AvailableCurrenciesEntity> list = new ArrayList<>();
        List<String> netList = new ArrayList<>();
        List<CurrencyChainInfoEntity> chainList = currencyChainInfoMapper.getAllChainInfo();
        for(CurrencyChainInfoEntity entity : chainList){
            String net = entity.getNet();
            netList.add(net);
        }
        //调用exchange now API获取可用币种接口
        String availablePairsUrl = AvailableCurrenciesUrl+"?active=true&flow=&buy=&sell=";
        JSONArray array = getExchangeNowResponse(availablePairsUrl);
        //对拉取回来的可用币种处理并入库
        for(Object obj : array){
            JSONObject tokenJson = JSONObject.parseObject(obj.toString());
            String network = tokenJson.get("network") == null ? "":tokenJson.get("network").toString().toUpperCase();
            if(netList.contains(network)){
                AvailableCurrenciesEntity entity = new AvailableCurrenciesEntity();
                String ticker = tokenJson.get("ticker") == null? "":tokenJson.get("ticker").toString().toUpperCase();
                String name = tokenJson.get("name") == null? "":tokenJson.get("name").toString();
                String image = tokenJson.get("image") == null? "":tokenJson.get("image").toString();
                entity.setNetwork(network);
                entity.setName(name);
                entity.setTicker(ticker);
                entity.setImage(image);
                entity.setCreateDate(new Date());
                list.add(entity);
            }
        }
        //每次清除旧有数据并从新拉取exchange now最新可以币种数据
        if(CollectionUtils.isNotEmpty(list)){
            availableCurrenciesMapper.remove();
            mybatisBatchInsertCurrencies(list);
        }
        return ResponseData.ok("跨链桥可用币插入成功");

    }


    /**
     * exchange now 跨链桥可用币种大数据量入库公共方法
     * @param dataList
     * @return
     */
    public long mybatisBatchInsertCurrencies(List<AvailableCurrenciesEntity> dataList) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        long beginTime = System.currentTimeMillis();
        try {
            AvailableCurrenciesMapper insertMapper = session.getMapper(AvailableCurrenciesMapper.class);
            for (AvailableCurrenciesEntity data : dataList) {
                insertMapper.insertAvailableCurrencies(data);
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
     * 获取exchange now 跨链桥可用可兑换币种
     * @return
     */
    public ResponseData getAvailableCurrencies(){
        List<CurrencyTypeEntity> list = currencyTypeMapper.getAvailableCurrencies();
        return ResponseData.ok(list);
    }


    /**
     * 获取exchange now跨链桥兑换币种 发出币最低兑换到的得到币数量
     * @param fromCurrency 发送币币种
     * @param fromNetwork  发送币所在公链
     * @param toCurrency   得到币币种
     * @param toNetwork    得到币所在公链
     * @return
     */
    public ResponseData  getMinAmount(String fromCurrency ,String fromNetwork,String toCurrency,String toNetwork){
        JSONObject json = new JSONObject();
        if(StringUtils.isEmpty(fromCurrency)||StringUtils.isEmpty(fromNetwork)||StringUtils.isEmpty(toCurrency)||StringUtils.isEmpty(toNetwork)){
            return  ResponseData.fail("参数缺失！");
        }

        //调用exchange now API发出币最低兑换到的得到币数量接口
        String minAmountUrl = MINAMOUNTURL+"?fromCurrency="+fromCurrency.toLowerCase()+"&toCurrency="+toCurrency.toLowerCase()+"&fromNetwork="+fromNetwork.toLowerCase()+"&toNetwork="+toNetwork.toLowerCase()+"&flow=";
        JSONObject array = getResponse(minAmountUrl);
        if(array != null ){
            String minAmount = array.get("minAmount") == null ? "0" :new BigDecimal(array.get("minAmount").toString()).toPlainString();
            String flow = array.get("flow") == null? "":array.get("flow").toString();
            json.put("minAmount",minAmount);
            json.put("flow",flow);
        }
        json.put("fromCurrency",fromCurrency);
        json.put("fromNetwork",fromNetwork);
        json.put("toCurrency",toCurrency);
        json.put("toNetwork",toNetwork);
        return ResponseData.ok(json);
    }


    /**
     * 获取exchange now跨链桥兑换币种 发出币最低兑换到的得到币数量  - 正反方向
     * @param fromCurrency 发送币币种
     * @param fromNetwork  发送币所在公链
     * @param toCurrency   得到币币种
     * @param toNetwork    得到币所在公链
     * @return
     */
    public ResponseData  getPositiveNegativeAmount(String fromCurrency ,String fromNetwork,String toCurrency,String toNetwork){
        JSONObject json = new JSONObject();
        if(StringUtils.isEmpty(fromCurrency)||StringUtils.isEmpty(fromNetwork)||StringUtils.isEmpty(toCurrency)||StringUtils.isEmpty(toNetwork)){
            return  ResponseData.fail("参数缺失！");
        }

        //调用exchange now API发出币最低兑换到的得到币数量接口
        //正向数量
        String positiveAmountUrl = MINAMOUNTURL+"?fromCurrency="+fromCurrency.toLowerCase()+"&toCurrency="+toCurrency.toLowerCase()+"&fromNetwork="+fromNetwork.toLowerCase()+"&toNetwork="+toNetwork.toLowerCase()+"&flow=";

        //反向数量
        String oppositeAmountUrl = MINAMOUNTURL+"?fromCurrency="+toCurrency.toLowerCase()+"&toCurrency="+fromCurrency.toLowerCase()+"&fromNetwork="+toNetwork.toLowerCase()+"&toNetwork="+fromNetwork.toLowerCase()+"&flow=";
        JSONObject array = getResponse(positiveAmountUrl);
        JSONObject oppositeArray = getResponse(oppositeAmountUrl);
        //正向兑换金额获取
        if(array != null ){
            String minAmount = array.get("minAmount") == null ? "0" :new BigDecimal(array.get("minAmount").toString()).toPlainString();
            String flow = array.get("flow") == null? "":array.get("flow").toString();
            json.put("minAmount",minAmount);
            json.put("flow",flow);
        }
        //反向兑换金额获取
        if(oppositeArray != null ){
            String oppositeAmount = oppositeArray.get("minAmount") == null ? "0" :new BigDecimal(oppositeArray.get("minAmount").toString()).toPlainString();
            json.put("oppositeAmount",oppositeAmount);
        }
        json.put("fromCurrency",fromCurrency);
        json.put("fromNetwork",fromNetwork);
        json.put("toCurrency",toCurrency);
        json.put("toNetwork",toNetwork);
        return ResponseData.ok(json);
    }


    /**
     * 调用exchange now接口的公用方法
     * @return
     */
    public JSONObject getResponse(String url){
        JSONObject data = new JSONObject();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-changenow-api-key", ApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);
            String  jsonStr = JSON.parse(response.getBody()).toString();
            data= JSONObject.parseObject(jsonStr);
        } catch (Exception e) {
            return data;
        }
        return data;
    }

    /**
     * 获取exchange now跨链桥兑换币种 发出币预计兑换到的得到币数量
     * @param fromCurrency 发送币币种
     * @param fromNetwork  发送币所在公链
     * @param toCurrency   得到币币种
     * @param toNetwork    得到币所在公链
     * @return
     */
    public ResponseData  getEtimatedAmount(String fromCurrency ,String fromNetwork,String toCurrency,String toNetwork,BigDecimal fromAmount){
        JSONObject array = new JSONObject();
        if(StringUtils.isEmpty(fromCurrency)||StringUtils.isEmpty(fromNetwork)||StringUtils.isEmpty(toCurrency)||StringUtils.isEmpty(toNetwork)){
            return  ResponseData.fail("参数缺失！");
        }

        //调用exchange now API发出币最低兑换到的得到币数量接口
        String minAmountUrl = ESTIMATEDAMOUNTURL+"?fromCurrency="+fromCurrency.toLowerCase()+"&toCurrency="+toCurrency.toLowerCase()+"&fromAmount="+fromAmount+"&toAmount=&fromNetwork="+fromNetwork.toLowerCase()+"&toNetwork="+toNetwork.toLowerCase()+"&flow=standard&type=direct&useRateId=false";

        array = getResponse(minAmountUrl);
        if(array != null ){
            String depositFee = array.get("depositFee") == null ? "0" :new BigDecimal(array.get("depositFee").toString()).toPlainString();
            String withdrawalFee = array.get("withdrawalFee") == null ? "0" :new BigDecimal(array.get("withdrawalFee").toString()).toPlainString();
            String from = array.get("fromAmount") == null ? "0" :new BigDecimal(array.get("fromAmount").toString()).toPlainString();
            String toAmount = array.get("toAmount") == null ? "0" :new BigDecimal(array.get("toAmount").toString()).toPlainString();
            array.put("depositFee",depositFee);
            array.put("withdrawalFee",withdrawalFee);
            array.put("fromAmount",from);
            array.put("toAmount",toAmount);

            array.put("fromCurrency",fromCurrency);
            array.put("fromNetwork",fromNetwork);
            array.put("toCurrency",toCurrency);
            array.put("toNetwork",toNetwork);
        }
        return ResponseData.ok(array);
    }


    /**
     * 跨链桥 - 调用exchange now跨链桥创建兑换交易
     * @param vo 创建兑换交易参数
     * @return
     */
    public ResponseData  createExchangeTransaction(ExchangeTransactionParamerVO vo){
        ExchangeRestltVO exchangeRestltVO = new ExchangeRestltVO();
        String fromCurrency = vo.getFromCurrency();
        String toCurrency = vo.getToCurrency();
        String fromNetwork = vo.getFromNetwork();
        String toNetwork = vo.getToNetwork();
        if(StringUtils.isEmpty(fromCurrency)||StringUtils.isEmpty(toCurrency)||StringUtils.isEmpty(fromNetwork)||StringUtils.isEmpty(toNetwork)){
            ResponseData.fail("创建兑换交易参数缺失！");
        }
        vo.setFromCurrency(fromCurrency.toLowerCase());
        vo.setFromNetwork(fromNetwork.toLowerCase());
        vo.setToCurrency(toCurrency.toLowerCase());
        vo.setToNetwork(toNetwork.toLowerCase());

        ExchangeTransactionRestltVO resultVO = new ExchangeTransactionRestltVO();
        //调用exchange now API获取可用币种接口
        String url = EXCHANGEURL;
        JSONObject data = new JSONObject();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-changenow-api-key", ApiKey);
            HttpEntity<ExchangeTransactionParamerVO> request = new HttpEntity<>(vo,headers);
            ResponseEntity<ExchangeTransactionRestltVO> result = restTemplate.postForEntity(url,request,ExchangeTransactionRestltVO.class);
            resultVO = result.getBody();
            resultVO.setBusinessId(vo.getBusinessId());
            resultVO.setCallbackUrl(vo.getCallbackUrl());
            String sign = getEncryptionData(resultVO);
            resultVO.setSign(sign);
            //20230409新增，用来处理兑换的确认页面的科学计数法
            BeanUtils.copyProperties(resultVO, exchangeRestltVO);
            String fromAmount = resultVO.getFromAmount() == null ? "0" : resultVO.getFromAmount().stripTrailingZeros().toPlainString();
            String toAmount = resultVO.getToAmount() == null ? "0" : resultVO.getToAmount().stripTrailingZeros().toPlainString();
            exchangeRestltVO.setFromAmount(fromAmount);
            exchangeRestltVO.setToAmount(toAmount);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用exchange now跨链桥创建兑换交易执行失败"+ JSONObject.toJSONString(vo)+"========"+JSONObject.toJSONString(resultVO));
            return ResponseData.fail("当前兑换网络波动过大，请重新设置兑换数量！");
        }

        return ResponseData.ok(exchangeRestltVO);
    }


    /**
     * 跨链桥 - 调用exchange now跨链桥创建兑换交易
     * @param vo 兑换交易参数vo
     * @return
     */
    public ExchangeTransactionRestltVO  getExchangeTransaction(ExchangeTransactionParamerVO vo){

        ExchangeTransactionRestltVO resultVO = new ExchangeTransactionRestltVO();
        JSONObject array = new JSONObject();
        //调用exchange now API获取可用币种接口
        String url = EXCHANGEURL;

        JSONObject data = new JSONObject();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-changenow-api-key", ApiKey);
            HttpEntity<ExchangeTransactionParamerVO> request = new HttpEntity<>(vo,headers);
            ResponseEntity<ExchangeTransactionRestltVO> result = restTemplate.postForEntity(url,request,ExchangeTransactionRestltVO.class);
            resultVO = result.getBody();
            String sign = getEncryptionData(resultVO);
            resultVO.setSign(sign);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("调用exchange now跨链桥创建兑换交易执行失败"+ JSONObject.toJSONString(vo)+"========"+JSONObject.toJSONString(resultVO));
            return resultVO;
        }
        return resultVO;
    }

    /**
     * 调用sha256生成加密交易sign
     * @param vo
     * @return
     */
    public String getEncryptionData(ExchangeTransactionRestltVO vo){
        Calendar calendar = Calendar.getInstance();
        Long createTime = calendar.getTimeInMillis();
        ExchangeTransactionEntity entity = new ExchangeTransactionEntity();
        BeanUtils.copyProperties(vo, entity);
        RandomGUID myGUID = new RandomGUID();
        String guid = myGUID.toString();
        entity.setOrderId(guid);
        entity.setExchangeOrderId(vo.getId());
        entity.setCreateTime(createTime);
//        entity.setTransactionStatus(1);
        //0329修改状态为预创建订单状态 - 已和需求确认
        entity.setTransactionStatus(Constant.exchange_precreate);
        entity.setFromCurrency(vo.getFromCurrency().toUpperCase());
        entity.setToCurrency(vo.getToCurrency().toUpperCase());
        entity.setToNetwork(vo.getToNetwork().toUpperCase());
        entity.setFromNetwork(vo.getFromNetwork().toUpperCase());
        exchangeTransactionMapper.insert(entity);
        messagePushFailService.sendMessage(entity);
        String key = Constant.sha256_key;
        Map<String,String> map = new HashMap<>();
        //加密数据顺序：fromCurrency=trx&fromNetwork=trx&payinAddress=TPyiY52atp2YedhqU25DKAkozb6G3R3SdL&payoutAddress=TC1HVcoGrVc5wTABVGDYHjFRCMTB3hQUJ5&toCurrency=usdt&toNetwork=trx&
        map.put("fromCurrency",vo.getFromCurrency());
        map.put("toCurrency",vo.getToCurrency());
        map.put("fromNetwork",vo.getFromNetwork());
        map.put("toNetwork",vo.getToNetwork());
        map.put("payinAddress",vo.getPayinAddress());
        map.put("payoutAddress",vo.getPayoutAddress());
//        map.put("fromCurrency","eth");
//        map.put("toCurrency","usdt");
//        map.put("fromNetwork","eth");
//        map.put("toNetwork","eth");
//        map.put("payinAddress","0x2DB8626B613ef92f3676cbA7702065c590Ecf4ba");
//        map.put("payoutAddress","0x5e16932a908B677Be19F172beeAB68430B950A6d");

        String sign = generateSignature(map,key);
        return sign;
    }

    /**
     * 生成签名（SHA256）
     *
     * @param data   待签名数据
     * @param appKey API密钥
     * @return 签名
     */
    public static String generateSignature(final Map<String, String> data, String appKey) {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);
        StringBuilder sb = new StringBuilder();
        for (String k : keyArray) {
            if ("sign".equals(k)) {
                continue;
            }
            if (data.get(k) instanceof String) {
                // 参数值为空，则不参与签名
                if (data.get(k).trim().length() > 0) {
                    sb.append(k).append("=").append(data.get(k).trim()).append("&");
                }
            }
        }
        sb.append("key=").append(appKey);
        return SHA256Utils.getSHA256(sb.toString());
    }


    /**
     * 根据跨链桥订单Id获取跨链桥订单交易状态
     * 订单创建超过两小时还未处理的视为交易订单失败
     */
    @Async
    public ResponseData updateTransactionStatus(){
        Calendar calendar = Calendar.getInstance();
        Long updateTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY, -2);
        Long time = calendar.getTimeInMillis();
        //处理两个小时以外的订单
        updateTransactionTwoHourStatus(time);
        List<ExchangeTransactionEntity> list = exchangeTransactionMapper.getExchangeTransactionList(time);
        if(CollectionUtils.isNotEmpty(list)){
            for(ExchangeTransactionEntity entity : list){
                String exchangeOrderId = entity.getExchangeOrderId();
                String url = TRANSACTIONSTATUSURL+"?id="+exchangeOrderId;
                JSONObject object = getTransactionStatusResponse(url);
                String status = object.get("status").toString();
                if(Constant.finished.equals(status)||Constant.failed.equals(status)){
                    ExchangeTransactionEntity transaction = new ExchangeTransactionEntity();
                    BigDecimal amountFrom = object.get("amountFrom") == null ? new BigDecimal("0") :new BigDecimal(object.get("amountFrom").toString());
                    BigDecimal amountTo = object.get("amountTo") == null ? new BigDecimal("0") :new BigDecimal(object.get("amountTo").toString());
                    String payinHash = object.get("payinHash") == null? "":object.get("payinHash").toString();
                    String payoutHash = object.get("payoutHash") == null? "":object.get("payoutHash").toString();

                    if(Constant.finished.equals(status)){
                        transaction.setTransactionStatus(5);
                    }else if(Constant.failed.equals(status)){
                        transaction.setTransactionStatus(6);
                    }
                    transaction.setId(entity.getId());
                    transaction.setFromAmount(amountFrom);
                    transaction.setToAmount(amountTo);
                    transaction.setPayinHash(payinHash);
                    transaction.setPayoutHash(payoutHash);
                    transaction.setUpdateTime(updateTime);
                    exchangeTransactionMapper.updateById(transaction);
                    //状态变更后推送消息给钱包
                    ExchangeTransactionEntity messageEntity = exchangeTransactionMapper.selectById(transaction.getId());
                    try{
                        messagePushFailService.sendMessage(messageEntity);
                    }catch (Exception e) {
                        e.printStackTrace();
                        log.error("获取两个小时以内的状态未成功和失败的交易数据修改状态是推送消息失败！"+ JSONObject.toJSONString(messageEntity));
                        continue;
                    }
                }
           }
        }
        return ResponseData.ok("修改交易状态成功");
    }



    public void updateTransactionTwoHourStatus(Long time){
        //获取所有两个小时以外的交易订单
        List<ExchangeTransactionEntity> list = exchangeTransactionMapper.getTransactionTwoHour(time);
        if(CollectionUtils.isNotEmpty(list)){
            for(ExchangeTransactionEntity entity : list){
                ExchangeTransactionEntity transaction = new ExchangeTransactionEntity();
                transaction.setId(entity.getId());
                transaction.setTransactionStatus(6);
                exchangeTransactionMapper.updateById(transaction);
                //状态变更后推送消息给钱包
                ExchangeTransactionEntity messageEntity = exchangeTransactionMapper.selectById(transaction.getId());
                try{
                    messagePushFailService.sendMessage(messageEntity);
                }catch (Exception e) {
                    e.printStackTrace();
                    log.error("获取两个小时以外的状态未成功和失败的交易数据修改状态是推送消息失败！"+ JSONObject.toJSONString(messageEntity));
                    continue;
                }
            }
        }
    }

    /**
     * 调用exchange now接口的公用方法
     * @return
     */
    public JSONObject getTransactionStatusResponse(String url){
        JSONObject data = new JSONObject();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-changenow-api-key", ApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);
            String  jsonStr = JSON.parse(response.getBody()).toString();
            data= JSONObject.parseObject(jsonStr);
        } catch (Exception e) {
            return data;
        }
        return data;
    }


}