package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.ExchangeTransactionEntity;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.management.TransactionParamerVO;


import java.util.Map;

/**
 * exchange now跨链桥订单信息表
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-15 11:55:29
 */
public interface ExchangeTransactionService extends IService<ExchangeTransactionEntity> {

    public ResponseData decryptTransactionInfo(String encodedString);

    /**
     * 兑换列表查询接口  - exchange now
     * @param vo 查询参数
     * @return
     */
    public ResponseData getHistoryExchangeTransactionList(TransactionParamerVO vo);


    /**
     * 根据订单Id获取兑换详情
     * @param orderId
     * @return
     */
    public ResponseData getExchangeTransactionByOrderId(String orderId);

    /**
     * 根据订单Id修改兑换订单状态
     * @param orderId
     * @return
     */
    public ResponseData updateTransactionByOrderId(String orderId);


    /**
     * 后台-首页统计-基础数据看板-今日做兑换业务的会员总数据
     * @return
     */
    public ResponseData getToDayExchangeUser();



    /**
     * 后台-首页统计-基础数据看板-今日兑换得到的枚数
     * @return
     */
    public ResponseData getExchangeSymbolNumber();


    /**
     * 后台-首页统计-基础数据看板-今日做兑换业务的笔数
     * @return
     */
    public ResponseData getTransactionCount();
}

