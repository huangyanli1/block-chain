package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.entity.CurrencyRateEntity;
import com.block.chain.entity.MessagePushFailEntity;
import com.block.chain.mapper.*;
import com.block.chain.service.CurrencyRateService;
import com.block.chain.service.MessagePushFailService;
import com.block.chain.utils.Constant;
import com.block.chain.utils.ResponseData;
import com.block.chain.utils.ToolUtils;
import com.block.chain.vo.CurrencyRateVO;
import com.block.chain.vo.ExchangeRateVO;
import com.block.chain.vo.management.FiatRateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("currencyRateService")
@Slf4j
public class CurrencyRateServiceImpl extends ServiceImpl<CurrencyRateMapper, CurrencyRateEntity> implements CurrencyRateService {
    @Autowired
    private SysRateMapper sysRateMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CurrencyRateMapper currencyRateMapper;

    @Autowired
    private QuoteCurrencyMapper quoteCurrencyMapper;

    @Autowired
    private CurrencyPriceMapper currencyPriceMapper;

    @Autowired
    private MessagePushFailService messagePushFailService;

    @Autowired
    private MessagePushFailMapper messagePushFailMapper;

    @Value("${RATE.FOREXRATEURL}")
    private String forexRateUrl;

    @Value("${RATE.ACCESSKEY}")
    private String accessKey;

    @Value("${RATE.APILAYERURL}")
    private String apilayerUrl;

    @Value("${BLOCKCC.RateUrl}")
    private String rateUrl;

    @Value("${BLOCKCC.ApiKey}")
    private String apiKey;

    @Value("${MESSAGE.WOWSENDURL}")
    private String wowsendUrl;




    /**
     * 获取所有法币，并集成法币汇率
     * @return
     */
    @Async
    public ResponseData integratedRate(){
        log.info("所有汇率开始进行集成");
        List<String> list = sysRateMapper.getAllSymbol();
        //获取所有计价的计价货币
        List<String> quoteList =quoteCurrencyMapper.getAllQuoteCurrency();
        if(CollectionUtils.isNotEmpty(list)&&CollectionUtils.isNotEmpty(quoteList)){
            list.stream()
                    .flatMap(i -> quoteList.stream().map(j -> i + " " + j))
                    .map(sum -> sum.split(" ")) // 将货币对拆分成两个货币
                    .forEach(currencies ->{
                        try{
                        processingRate(currencies[0], currencies[1]);//已拆解源货币和计价货币调用processingRate方法
                        }catch (Exception e){
                            e.printStackTrace();
                            log.error("获取所有法币，并集成法币汇率失败sourceCurrency=="+ currencies[0] +"==quoteCurrency=="+ currencies[1]);
                            return; // 跳过当前货币对并继续处理下一个货币对
                        }

                    });
            log.info("所有汇率已经集成完成！");
        }
        List<CurrencyRateVO> rateList = currencyRateMapper.getFiatList();
        //汇率变动，推送消息
        if(CollectionUtils.isNotEmpty(rateList)){
            messagePushFailService.sendCurrencyRateMessage(rateList);
        }
        return ResponseData.ok("汇率集成完成");
    }

    /**
     * 如果已经处理汇率则返回TRUE否则返回FALSE
     * @param sourceCurrency
     * @return
     */
    public Boolean processingRate(String sourceCurrency,String quoteCurrency){
        if(sourceCurrency.equals(quoteCurrency)){
            return false;
        }
        Calendar cal = Calendar.getInstance();
        Long time = cal.getTimeInMillis();
        Boolean isRate= false;
        try{
        String url =forexRateUrl+"?fromCode="+sourceCurrency+"&toCode="+quoteCurrency;
        JSONObject obj = getData(url);
        String code = obj.get("code") == null ? "" : obj.get("code").toString();
        if(StringUtils.isNotEmpty(code)&&"0".equals(code)){
            JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(obj.get("data")));
            BigDecimal  rate = data.get("rate") ==null ? null : new BigDecimal(data.get("rate").toString());
            BigDecimal one = new BigDecimal("1");
            BigDecimal reverseRate = one.divide(rate,4, RoundingMode.HALF_UP);
            List<CurrencyRateEntity> forwardList = currencyRateMapper.getCurrencyRate(sourceCurrency,quoteCurrency);
            if(CollectionUtils.isNotEmpty(forwardList)){
                CurrencyRateEntity entity = forwardList.get(0);
                entity.setRate(reverseRate);
                entity.setUpdateTime(time);
                currencyRateMapper.updateById(entity);
            }else{
                CurrencyRateEntity entity = new CurrencyRateEntity();
                entity.setSourceCurrency(sourceCurrency);
                entity.setQuoteCurrency(quoteCurrency);
                entity.setCreateTime(time);
                entity.setRate(reverseRate);
                currencyRateMapper.insert(entity);
            }
            List<CurrencyRateEntity> reverseList = currencyRateMapper.getCurrencyRate(quoteCurrency,sourceCurrency);
            if(CollectionUtils.isNotEmpty(reverseList)){
                CurrencyRateEntity entity = reverseList.get(0);
                entity.setRate(rate);
                entity.setUpdateTime(time);
                currencyRateMapper.updateById(entity);
            }else{
                CurrencyRateEntity entity = new CurrencyRateEntity();
                entity.setSourceCurrency(quoteCurrency);
                entity.setQuoteCurrency(sourceCurrency);
                entity.setCreateTime(time);
                entity.setRate(rate);
                currencyRateMapper.insert(entity);
            }
            isRate = true;
        }
    }catch (Exception e){
        e.printStackTrace();
        log.error("集成汇率的具体实现时失败sourceCurrency=="+ sourceCurrency +"==quoteCurrency=="+ quoteCurrency);
        return isRate;
    }
        return isRate;
    }


    /**
     * 获取汇率的请求方法
     * @return
     */
    public JSONObject getData(String url){
        JSONObject data = new JSONObject();
        log.info("开始进行汇率请求="+url);
        try{
            Object result = restTemplate.getForObject(url,Object.class);
            data = JSONObject.parseObject(JSONObject.toJSONString(result));
            Thread.sleep(7000);
        } catch (Exception e) {
//            log.error("取汇率的请求失败url=="+url);
            return data;
        }
        return data;
    }





    /**
     * 获取法币汇率，需要接入外部数据
     * @return
     */
    public ResponseData getFiatRate(String sourceCurrency,String quoteCurrency){
        CurrencyRateVO vo = new CurrencyRateVO();
        if(StringUtils.isEmpty(sourceCurrency)&&StringUtils.isEmpty(quoteCurrency)){
            return ResponseData.fail("查询参数缺失！");
        }
        List<CurrencyRateEntity> list = currencyRateMapper.getCurrencyRate(sourceCurrency,quoteCurrency);
        if(CollectionUtils.isNotEmpty(list)){
            BeanUtils.copyProperties(list.get(0), vo);
        }
        return ResponseData.ok(vo);
    }


    /**
     * 获取所有监控了汇率的法币列表
     * @return
     */
    public ResponseData getFiatList(){
        List<CurrencyRateVO> list =  currencyRateMapper.getFiatList();
        return ResponseData.ok(list);
    }


    /**
     * 获取所有监控了价格的数字货币列表
     * @return
     */
    public ResponseData getALLCurrencyPrice(){
        List<CurrencyPriceEntity> list = currencyPriceMapper.getALLCurrencyPrice();
        return ResponseData.ok(list);
    }

    /**
     * 获取所有法币，并集成法币汇率 - 汇率集成的补充接口
     * @return
     */
    @Async
    public ResponseData integratedSupplementaryRate(){
        //获取所有需要查询汇率的法币
        List<String> list = sysRateMapper.getAllSymbol();
        //获取所有的计价货币
        List<String> quouteList = quoteCurrencyMapper.getAllQuoteCurrency();
        String currencies="";
        if(CollectionUtils.isNotEmpty(list)){
            //拼接所有的法币，并用逗号分隔
            currencies = list.stream().collect(Collectors.joining(", "));
        }
        //循环所有的计价货币，筛选每一种计价货币对应的所有法币汇率
        for(String quoteSymbol : quouteList){
            String url =apilayerUrl+"?access_key="+accessKey+"&currencies="+currencies+"&source="+quoteSymbol+"&format=1";
            try{
                JSONObject obj = getData(url);
                JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(obj.get("quotes")));
                //处理获取返回的汇率信息
                jsonObject.keySet().forEach(key -> {
                    Calendar cal = Calendar.getInstance();
                    Long time = cal.getTimeInMillis();
                    Object value = jsonObject.get(key);
                    //计价货币
                    String quoteCurrency = key.substring(0, 3);
                    //源货币
                    String sourceCurrency = key.substring(3);
                    //quoteCurrency - sourceCurrency = rate
                    BigDecimal  rate = value ==null ? null : new BigDecimal(value.toString());

                    BigDecimal one = new BigDecimal("1");
                    BigDecimal reverseRate = one.divide(rate,4, RoundingMode.HALF_UP);
                    List<CurrencyRateEntity> forwardList = currencyRateMapper.getCurrencyRate(sourceCurrency,quoteCurrency);
                    if(CollectionUtils.isNotEmpty(forwardList)){
                        CurrencyRateEntity entity = forwardList.get(0);
                        entity.setRate(reverseRate);
                        entity.setUpdateTime(time);
                        currencyRateMapper.updateById(entity);
                    }else{
                        CurrencyRateEntity entity = new CurrencyRateEntity();
                        entity.setSourceCurrency(sourceCurrency);
                        entity.setQuoteCurrency(quoteCurrency);
                        entity.setCreateTime(time);
                        //sourceCurrency - quoteCurrency = reverseRate
                        entity.setRate(reverseRate);
                        currencyRateMapper.insert(entity);
                    }
                    List<CurrencyRateEntity> reverseList = currencyRateMapper.getCurrencyRate(quoteCurrency,sourceCurrency);
                    if(CollectionUtils.isNotEmpty(reverseList)){
                        CurrencyRateEntity entity = reverseList.get(0);
                        entity.setRate(rate);
                        entity.setUpdateTime(time);
                        currencyRateMapper.updateById(entity);
                    }else{
                        CurrencyRateEntity entity = new CurrencyRateEntity();
                        entity.setSourceCurrency(quoteCurrency);
                        entity.setQuoteCurrency(sourceCurrency);
                        entity.setCreateTime(time);
                        //quoteCurrency - sourceCurrency = rate
                        entity.setRate(rate);
                        currencyRateMapper.insert(entity);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                log.error("获取所有法币，并集成法币汇率 - 汇率集成的补充接口请求失败 url=="+url);
                continue;
            }
        }
        return ResponseData.ok("汇率集成完成");
    }


    /**
     * 通过法币获取对应美元的汇率 - 该接口只针对美元汇率
     * @return
     */
    public ResponseData exchangeRate(String target){
        ExchangeRateVO vo = new ExchangeRateVO();
        if(StringUtils.isNotEmpty(target)){
            target = target.toUpperCase();
            vo = currencyRateMapper.exchangeRate(Constant.USD,target);
            String sourceCurrency = vo.getSource() == null ? "" : vo.getSource().toLowerCase();
            String quoteCurrency = vo.getTarget() == null ? "" : vo.getTarget().toLowerCase();
            vo.setSource(sourceCurrency);
            vo.setTarget(quoteCurrency);
        }
        return ResponseData.ok(vo);
    }



    /**
     * 更新所有法币对应的美元汇率 - 通过蜜蜂查进行更新集成
     * @return
     */
    @Async
    public ResponseData mifengchaExchangeRate() {
        Long time = System.currentTimeMillis();
        JSONArray data;
        String url = rateUrl + "?api_key=" + apiKey;
        try {
            Object result = restTemplate.getForObject(url, Object.class);
            data = JSONArray.parseArray(JSONArray.toJSONString(result));
        } catch (Exception e) {
            log.error("蜜蜂查获取所有法币对美元汇率的请求失败url==" + url);
            return ResponseData.fail("获取汇率失败");
        }
        for (Object obj : data) {
            JSONObject json = (JSONObject) obj;
            String quoteCurrency = json.getString("c");
            BigDecimal rate = json.getBigDecimal("r");
            List<CurrencyRateEntity> rateList = currencyRateMapper.getCurrencyRate(Constant.USD, quoteCurrency);
            if (!rateList.isEmpty()) {
                CurrencyRateEntity rateEntity = rateList.get(0);
                rateEntity.setRate(rate);
                rateEntity.setUpdateTime(time);
                currencyRateMapper.updateById(rateEntity);
            } else {
                CurrencyRateEntity rateEntity = new CurrencyRateEntity();
                rateEntity.setRate(rate);
                rateEntity.setQuoteCurrency(quoteCurrency);
                rateEntity.setSourceCurrency(Constant.USD);
                rateEntity.setCreateTime(time);
                currencyRateMapper.insert(rateEntity);
            }
        }
        return ResponseData.ok("法币汇率更新完成");
    }


    /**
     * 推送所有更新的法币汇率 - 仅仅适用于消息推送
     * @return
     */
    @Async
    public ResponseData sendFiatRateMessage(){
        JSONObject json = new JSONObject();
        List<FiatRateVO> list = currencyRateMapper.messageFiatList();
        String entityData = "";
        if(CollectionUtils.isNotEmpty(list)){
            try{
                entityData = JSONArray.toJSONString(list);
//          JSONObject message = JSONObject.parseObject(entityData);
                json.put("data",entityData);
                json.put("messageType",Constant.ASSET_BROADCAST_FIAT);
                HttpEntity<JSONObject> request = new HttpEntity<>(json,null);
                Object result = restTemplate.postForObject(wowsendUrl,request,Object.class);
            }catch (Exception e){
                e.printStackTrace();
                //推送消息失败后将失败消息放入失败信息存储表，然后在后续继续发送消息
                MessagePushFailEntity failMessage = new MessagePushFailEntity();
                failMessage.setData(entityData);
                failMessage.setMessageType(Constant.ASSET_BROADCAST_FIAT);
                failMessage.setCreateDate(new Date());
                messagePushFailMapper.insert(failMessage);
                log.error("法币汇率变动发送消息失败entityData =="+ entityData +"=====messageType====="+Constant.ASSET_BROADCAST_FIAT);
            }
        }
        return ResponseData.ok(list);
    }


    /**
     * 通过法币获取对应美元的汇率 - 该接口只针对美元汇率
     * @return
     */
    public ResponseData getSymbolsRateList(String symbols){
        if(StringUtils.isEmpty(symbols)){
            return ResponseData.fail("查询参数有误！");
        }
        List<String> symbolList = new ArrayList<>();
        String[] symbolArray = ToolUtils.str2StrArray(symbols,",");
        for(String symbol : symbolArray){
            if(StringUtils.isNotEmpty(symbol)){
                symbolList.add(symbol.toUpperCase());
            }
        }
        List<FiatRateVO> list = currencyRateMapper.getSymbolRateList(symbolList);
        for(FiatRateVO vo : list){
            BigDecimal rateNumber = vo.getRate() == null ? new BigDecimal("0"): new BigDecimal(vo.getRate().toPlainString());
            vo.setRate(rateNumber);
        }
        return ResponseData.ok(list);
    }

    /**
     * 通过法币获取对应美元的汇率 - 该接口只针对美元汇率
     * @return
     */
    public ResponseData getSymbolsRate(String symbol){
        if(StringUtils.isEmpty(symbol)){
            return ResponseData.fail("查询参数有误！");
        }
        FiatRateVO rate = currencyRateMapper.getSymbolRate(symbol.toUpperCase());
        BigDecimal rateNumber = rate.getRate() == null ? new BigDecimal("0"): new BigDecimal(rate.getRate().toPlainString());
        rate.setRate(rateNumber);
        return ResponseData.ok(rate);
    }


}