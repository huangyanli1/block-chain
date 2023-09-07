package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.TransactionBtcInfoEntity;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.TransactionInfoVO;


import java.util.List;

/**
 * 公链BTC下交易记录
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-27 17:10:16
 */
public interface TransactionBtcInfoService extends IService<TransactionBtcInfoEntity> {

    /**
     * 动态录入交易记录信息
     * @param list 交易记录
     * @return
     */
    public ResponseData saveTransactionInfo(List<TransactionInfoVO> list);

    /**
     * 动态修改交易记录
     * @param list
     * @return
     */
    public ResponseData updateTransactionInfo(List<TransactionInfoVO> list);

    /**
     * 根据txHash和net动态修改交易记录
     * @param list
     * @return
     */
    public ResponseData updateTransactionInfoByTxHash(List<TransactionInfoVO> list);

    /**
     * 获取所有状态为pending的交易记录
     * @return
     */
    public ResponseData getPendinInfoList();

    /**
     * 后台管理系统-加密货币交易管理 - 接收币列表
     * @return
     */
    public ResponseData getReceiveTransactionList(List<String> address,String txHash,Integer isConfim,Long startTime,Long endTime,Integer page, Integer pageSize);


    /**
     * 后台管理系统-加密货币交易管理 - 发送币列表
     * @return
     */
    public ResponseData getSendingTransactionList(List<String> address,String symbol,Integer minDiff,Integer maxDiff,Long startTime,Long endTime,Integer page, Integer pageSize);


    /**
     * 根据交易hash和公链net 获取接收和发送详情
     * @return
     */
    public ResponseData getInfoByTxHash(String net ,String txHash);
}

