package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.WalletCurrencyTransactionEntity;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.management.TransactionParamerVO;


import java.util.List;
import java.util.Map;

/**
 * 币币兑换交易订单信息表
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-12-08 14:55:59
 */
public interface WalletCurrencyTransactionService extends IService<WalletCurrencyTransactionEntity> {

    /**
     * 币币兑换订单录入接口 - 对接去中心化交易所
     * @param entity 订单交易信息
     * @return
     */
    public ResponseData saveCurrencyTransactionInfo(WalletCurrencyTransactionEntity entity);


    /**
     * 兑换列表查询接口
     * @param vo 查询参数
     * @return
     */
    public ResponseData getCurrencyTransactionList(TransactionParamerVO vo);


    /**
     * 获取所有状态为broadcasting币币兑换交易数据
     * @return
     */
    public ResponseData getBroadcastingList();

    /**
     * 订单状态修改
     * @param list
     * @return
     */
    public ResponseData updateCurrencyTransaction(List<WalletCurrencyTransactionEntity> list);

    /**
     * 币币兑换交易信息 - 详情查询接口
     * @param id 币币兑换订单ID
     * @return
     */
    public ResponseData getCurrencyTransactionById(String id);
}

