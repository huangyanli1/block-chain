package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.WalletTransactionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 钱包交易
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-17 14:51:23
 */
@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransactionEntity> {

    /**
     * 获取所有交易状态为unconfirmed的交易数据
     * @return
     */
    List<WalletTransactionEntity> getAllUnconfirmedList();

    /**
     * 根据条件筛选交易信息
     * @param addressList 地址列表
     * @return
     */
    List<WalletTransactionEntity> getTransactionList(@Param("addressList") List<String> addressList);

    /**
     * 获取入账交易记录==地址在from或者to，且交易金额为正
     * @param addressList 地址列表
     * @return
     */
    List<WalletTransactionEntity> getEntriesTransactionList(@Param("addressList") List<String> addressList,@Param("startTime") Long startTime,@Param("endTime") Long  endTime,@Param("symbol") String  symbol);


    /**
     * 获取提现交易记录==地址在from或者to，且交易金额为负数
     * @param addressList 地址列表
     * @return
     */
    List<WalletTransactionEntity> getWithdrawalTransactionList(@Param("addressList") List<String> addressList,@Param("startTime") Long startTime,@Param("endTime") Long  endTime,@Param("symbol") String  symbol);


}
