package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.CurrencyPriceEntity;
import com.block.chain.entity.ExchangeTransactionEntity;
import com.block.chain.entity.MessagePushFailEntity;
import com.block.chain.vo.CurrencyRateVO;

import java.util.List;
/**
 * 推送消息失败信息存储表
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-06-09 14:32:27
 */
public interface MessagePushFailService extends IService<MessagePushFailEntity> {

    /**
     * 推送失败的消息从新推送
     */
    public void handleFailMessage();

    /**
     * 兑换交易发送消息的公共方法
     */
    public void sendMessage(ExchangeTransactionEntity entity);

    /**
     * 币种价格变动发送消息的公共方法
     */
    public void sendCurrencyPriceMessage(List<CurrencyPriceEntity> list);

    /**
     * 法币汇率变动发送消息的公共方法
     */
    public void sendCurrencyRateMessage(List<CurrencyRateVO> list);


}

