package com.block.chain.mapper;

import com.block.chain.entity.WalletCurrencyTransactionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.vo.TransactionBtcInfoVO;
import com.block.chain.vo.management.TransactionParamerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 币币兑换交易订单信息 Mapper类
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-12-08 14:55:59
 */
@Mapper
public interface WalletCurrencyTransactionMapper extends BaseMapper<WalletCurrencyTransactionEntity> {

    /**
     * 后台管理系统-加密货币交易管理 - 兑换币列表
     * @param vo 兑换查询信息
     * @return
     */
    public List<WalletCurrencyTransactionEntity> getCurrencyTransactionList(TransactionParamerVO vo);
    public Integer getCurrencyTransactionCount(TransactionParamerVO vo);

    /**
     * 获取所有状态为broadcasting币币兑换交易数据
     * @return
     */
    public List<WalletCurrencyTransactionEntity> getBroadcastingList();



    /**
     *  根据筛选条件获取对应 币币交易信息
     * @param vo
     * @return
     */
    public List<WalletCurrencyTransactionEntity> getCurrencyTransactionInfo(TransactionParamerVO vo);

    public List<WalletCurrencyTransactionEntity> getExchangeList(@Param("address")  List<String> address,@Param("net") String net);





}
