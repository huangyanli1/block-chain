package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.AvailablePairsEntity;
import com.block.chain.entity.CompanyAddressEntity;
import com.block.chain.entity.TransactionBtcInfoEntity;
import com.block.chain.entity.WalletTransactionEntity;
import com.block.chain.vo.AddressParameterVO;
import com.block.chain.vo.QuotationVO;
import com.block.chain.vo.TransactionBtcInfoVO;
import com.block.chain.vo.TransactionInfoVO;
import com.block.chain.vo.management.MemberSymbolVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公链BTC下交易记录
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-27 17:10:16
 */
@Mapper
public interface TransactionBtcInfoMapper extends BaseMapper<TransactionBtcInfoEntity> {

    /**
     * 公链交易记录录入
     * @param vo
     * @return
     */
    int  saveTransactionInfo(TransactionBtcInfoVO vo);

    /**
     * 公链交易记录修改
     * @param vo
     * @return
     */
    int updateTransactionInfo(TransactionBtcInfoVO vo);

    /**
     * 根据txHash和net动态修改公链交易记录
     * @param vo
     * @return
     */
    int updateTransactionInfoByTxHash(TransactionBtcInfoVO vo);

    /**
     * 动态获取所有公链交易记录中交易状态为pending的数据
     * @return
     */
    List<TransactionBtcInfoVO> getPendingTransactionList(TransactionBtcInfoVO vo);


    /**
     * 获取地址下交易记录公用方法 - 交易记录入库优化后 根据地址和公链到对应公链记录表中查询
     * @param vo 查询参数信息
     * @return
     */
    List<TransactionBtcInfoVO> geNetAddressTransaction(AddressParameterVO vo);

    /**
     * 获取地址下交易记录公用方法 - 交易记录入库优化后 根据地址和公链到对应公链记录表中查询
     * @param vo 查询参数信息
     * @return
     */
    List<TransactionBtcInfoVO> getListByHash(AddressParameterVO vo);

    /**
     * 后台管理系统 - 财务管理 - 公司钱包管理统计数据获取 -入账
     * @param address 地址
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    int getEntriesStatisticsTransaction(@Param("tableName") String tableName,@Param("address") String address,@Param("startTime") Long startTime,@Param("endTime") Long  endTime);
    List<TransactionBtcInfoVO> getEntriesStatisticsTransactionList(@Param("tableName") String tableName,@Param("address") String address,@Param("startTime") Long startTime,@Param("endTime") Long  endTime);
    /**
     * 后台管理系统 - 财务管理 - 公司钱包管理统计数据获取 -出账
     * @param address 地址
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    int getWithdrawalStatisticsTransaction(@Param("tableName") String tableName,@Param("address") String address,@Param("startTime") Long startTime,@Param("endTime") Long  endTime);
    List<TransactionBtcInfoVO> getWithdrawalStatisticsTransactionList(@Param("tableName") String tableName,@Param("address") String address,@Param("startTime") Long startTime,@Param("endTime") Long  endTime);


    /**
     * 后台管理系统 - 首页统计 - 接收统计
     * @param tableName
     * @param net
     * @param symbol
     * @param startTime
     * @param endTime
     * @return
     */
    List<TransactionBtcInfoVO> getCurrencyReceiveStatistics(@Param("tableName") String tableName,@Param("net") String net,@Param("symbol") String symbol,@Param("startTime") Long startTime,@Param("endTime") Long  endTime,@Param("address") String address);
    /**
     * 后台管理系统 - 首页统计 - 接收统计
     * @param tableName
     * @param net
     * @param symbol
     * @param startTime
     * @param endTime
     * @return
     */
    List<TransactionBtcInfoVO> getCurrencySendingStatistics(@Param("tableName") String tableName,@Param("net") String net,@Param("symbol") String symbol,@Param("startTime") Long startTime,@Param("endTime") Long  endTime);

    /**
     * 后台管理系统-加密货币交易管理 - 接收币列表
     * @param page
     * @param pageSize
     * @return
     */
    public List<TransactionBtcInfoVO> getReceiveList(@Param("addressList") List<String> addressList, @Param("txHash") String txHash,@Param("confim") Integer confim,
                                                                   @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("page") Integer page, @Param("pageSize") Integer pageSize);

    public Integer getReceiveCount(@Param("addressList") List<String> addressList, @Param("txHash") String txHash,@Param("confim") Integer confim,
                                           @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("page") Integer page, @Param("pageSize") Integer pageSize);


    /**
     * 后台管理系统-加密货币交易管理 - 发送币列表
     * @param page
     * @param pageSize
     * @return
     */
    public List<TransactionBtcInfoVO> getSendingList(@Param("addressList") List<String> addressList, @Param("symbol") String symbol,@Param("minDiff") Integer minDiff, @Param("maxDiff") Integer maxDiff,
                                                     @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("page") Integer page, @Param("pageSize") Integer pageSize);

    public Integer getSendingCount(@Param("addressList") List<String> addressList, @Param("symbol") String symbol,@Param("minDiff") Integer minDiff, @Param("maxDiff") Integer maxDiff,
                                   @Param("startTime") Long startTime, @Param("endTime") Long endTime,@Param("page") Integer page, @Param("pageSize") Integer pageSize);


    /**
     * 后台管理系统 - 会员管理 - 会员基础信息 - 加密数字资产管理 - 按net，address，symbol统计入账信息
     * @param tableName
     * @param net
     * @param address
     * @return
     */
    public List<MemberSymbolVO> getReceiveGroupBySymbol(@Param("tableName") String tableName, @Param("net") String net, @Param("address") String address);
    public List<MemberSymbolVO> getReceiveGroupBySymbols(@Param("tableName") String tableName, @Param("net") String net, @Param("address") String address);

    /**
     * 后台管理系统 - 会员管理 - 会员基础信息 - 加密数字资产管理 - 按net，address，symbol统计出账信息
     * @param tableName
     * @param net
     * @param address
     * @return
     */
    public List<MemberSymbolVO> getSendingGroupBySymbol(@Param("tableName") String tableName, @Param("net") String net, @Param("address") String address);
    public List<MemberSymbolVO> getSendingGroupBySymbols(@Param("tableName") String tableName, @Param("net") String net, @Param("address") String address);


    /**
     * 后台管理系统 - 首页统计 - 接收发送统计 - 历史接收币价值排名 - 历史接收币总价值获取
     * @param addressList
     * @return
     */
    public List<TransactionBtcInfoVO> getHistoryReceiveList(@Param("addressList") List<String> addressList);

    /**
     * 后台管理系统 - 首页统计 - 接收发送统计 - 历史接收币价值排名 - 历史发送币总价值获取
     * @param addressList
     * @return
     */
    public List<TransactionBtcInfoVO> getHistorySendingList(@Param("addressList") List<String> addressList);


    /**
     * 获取对应交易记录表下所有的交易记录，用来补全缺失的交易参数信息，如coinPrice,coinValue
     * @param tableName 表名
     * @return
     */
    public List<TransactionBtcInfoEntity> getTransactionByName(@Param("tableName") String tableName);



    /**
     * 批量插入exchange now可用币对
     * @param data
     * @return
     */
    int  updateData(@Param("tableName") String tableName , @Param("data") TransactionBtcInfoEntity data);

}
