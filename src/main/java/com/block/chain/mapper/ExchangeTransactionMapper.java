package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.ExchangeTransactionEntity;
import com.block.chain.entity.WalletCurrencyTransactionEntity;
import com.block.chain.vo.TransactionBtcInfoVO;
import com.block.chain.vo.management.TransactionParamerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * exchange now跨链桥订单信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-15 11:55:29
 */
@Mapper
public interface ExchangeTransactionMapper extends BaseMapper<ExchangeTransactionEntity> {

    /**
     * 获取两个小时以内的状态未成功和失败的交易数据
     * @param createTime
     * @return
     */
    List<ExchangeTransactionEntity> getExchangeTransactionList(@Param("createTime") Long createTime);

    /**
     * 获取两个小时以外的状态未成功和失败的交易数据
     * @param createTime 两个小时时间点
     * @return
     */
    List<ExchangeTransactionEntity> getTransactionTwoHour(@Param("createTime") Long createTime);


    /**
     * 后台管理系统 - 首页统计 - 接收发送统计 - 历史接收币价值排名 - 历史兑换币总价值获取
     * @param addressList
     * @return
     */
    public List<ExchangeTransactionEntity> getHistoryExchangeList(@Param("addressList") List<String> addressList);


    /**
     * 后台管理系统-加密货币交易管理 - 兑换币列表
     * @param vo 兑换查询信息
     * @return
     */
    public List<ExchangeTransactionEntity> getTransactionList(TransactionParamerVO vo);


    /**
     * exchange now历史兑换交易列表
     * @param vo 查询参数
     * @return
     */
    public List<ExchangeTransactionEntity> getHistoryExchangeTransactionList(TransactionParamerVO vo);
    public Integer getHistoryExchangeTransactionCount(TransactionParamerVO vo);


    /**
     * 根据订单Id获取兑换详情
     * @param orderId
     * @return
     */
    public ExchangeTransactionEntity getExchangeTransactionByOrderId(@Param("orderId") String orderId);


    /**
     * exchange now订单状态修改
     * @param entity
     * @return
     */
    int updateTransactionByOrderId(ExchangeTransactionEntity entity);

}
