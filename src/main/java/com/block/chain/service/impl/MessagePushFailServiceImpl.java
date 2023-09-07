package com.block.chain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.entity.ExchangeTransactionEntity;
import com.block.chain.entity.MessagePushFailEntity;
import com.block.chain.mapper.MessagePushFailMapper;
import com.block.chain.service.MessagePushFailService;
import com.block.chain.utils.Constant;
import com.block.chain.vo.CurrencyRateVO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service("messagePushFailService")
public class MessagePushFailServiceImpl extends ServiceImpl<MessagePushFailMapper, MessagePushFailEntity> implements MessagePushFailService {

    @Autowired
    private MessagePushFailMapper messagePushFailMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${MESSAGE.WOWSENDURL}")
    private String wowsendUrl;


    /**
     * 推送失败的消息从新推送
     */
    @Async
    public void handleFailMessage(){
        List<MessagePushFailEntity> list = messagePushFailMapper.getALLFailMessage();
        if(CollectionUtils.isNotEmpty(list)){
            for(MessagePushFailEntity entity : list){
                String entityData = entity.getData();
                String messageType = entity.getMessageType();
                try{
                    JSONObject json = new JSONObject();
//                    JSONObject message = JSONObject.parseObject(entityData);
                    json.put("data",entityData);
                    json.put("messageType", Constant.WOW_EXCHANGE);
                    HttpEntity<JSONObject> request = new HttpEntity<>(json,null);
                    Object obj = restTemplate.postForObject(wowsendUrl,request,Object.class);
                    JSONObject result = JSONObject.parseObject(obj.toString());
                    Integer code = Integer.valueOf(result.get("code").toString());
                    if(200 == code){
                        messagePushFailMapper.deleteById(entity.getId());
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    log.error("推送失败的消息从新推送entityData =="+ entityData +"=====messageType====="+messageType);
                    continue;
                }
            }
        }
    }


    /**
     * 兑换交易发送消息的公共方法
     */
    @Async
    public void sendMessage(ExchangeTransactionEntity entity){
        JSONObject json = new JSONObject();
        String entityData = "";
        try{
            entityData = JSONObject.toJSONString(entity);
            JSONObject message = JSONObject.parseObject(entityData);
            json.put("data",message);
            json.put("messageType",Constant.WOW_EXCHANGE);
            HttpEntity<JSONObject> request = new HttpEntity<>(json,null);
            Object result = restTemplate.postForObject(wowsendUrl,request,Object.class);
        }catch (Exception e){
            e.printStackTrace();
            //推送消息失败后将失败消息放入失败信息存储表，然后在后续继续发送消息
            MessagePushFailEntity failMessage = new MessagePushFailEntity();
            failMessage.setData(entityData);
            failMessage.setMessageType(Constant.WOW_EXCHANGE);
            failMessage.setCreateDate(new Date());
            messagePushFailMapper.insert(failMessage);
            log.error("兑换交易推送消息失败entityData =="+ entityData +"=====messageType====="+Constant.WOW_EXCHANGE);
        }
    }

    /**
     * 币种价格变动发送消息的公共方法
     */
    @Async
    public void sendCurrencyPriceMessage(List<CurrencyPriceEntity> list){
        try{
            int size = list.size();
            List<CurrencyPriceEntity> firstHalf = list.stream().limit(size/2).collect(Collectors.toList());
            List<CurrencyPriceEntity> secondHalf = list.stream().skip(size/2).collect(Collectors.toList());
            handlePriceMessage(firstHalf);
            handlePriceMessage(secondHalf);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void handlePriceMessage(List<CurrencyPriceEntity> list){
        JSONObject json = new JSONObject();
        String entityData = "";
        try{
            entityData  = JSONArray.toJSONString(list);
            json.put("data",entityData);
            json.put("messageType",Constant.ASSET_BROADCAST_CRYPTO);
            HttpEntity<JSONObject> request = new HttpEntity<>(json,null);
            Object result = restTemplate.postForObject(wowsendUrl,request,Object.class);
        }catch (Exception e){
            e.printStackTrace();
            //推送消息失败后将失败消息放入失败信息存储表，然后在后续继续发送消息
            MessagePushFailEntity failMessage = new MessagePushFailEntity();
            failMessage.setData(entityData);
            failMessage.setMessageType(Constant.ASSET_BROADCAST_CRYPTO);
            failMessage.setCreateDate(new Date());
            messagePushFailMapper.insert(failMessage);
            log.error("币种价格变动发送消息失败entityData =="+ entityData +"=====messageType====="+Constant.ASSET_BROADCAST_CRYPTO);
        }


    }

    /**
     * 法币汇率变动发送消息的公共方法
     */
    @Async
    public void sendCurrencyRateMessage(List<CurrencyRateVO> list){
        JSONObject json = new JSONObject();
        String entityData = "";
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

}