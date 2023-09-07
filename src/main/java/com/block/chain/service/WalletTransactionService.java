package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.WalletTransactionEntity;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.AddressParameterVO;
import com.block.chain.vo.TransactionParamsVO;

import java.util.List;
import java.util.Map;

/**
 * 钱包交易
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-17 14:51:23
 */
public interface WalletTransactionService extends IService<WalletTransactionEntity> {

    /**
     * 钱包交易记录保存
     * @param entity 钱包交易记录信息
     * @return
     */
    public ResponseData saveWalletTransaction(WalletTransactionEntity entity);

    /**
     * 钱包交易记录修改
     * @param entity
     * @return
     */
    public ResponseData  updateWalletTransaction(WalletTransactionEntity entity);

    /**
     * 获取所有交易状态为unconfirmed的交易数据
     * @return
     */
    public ResponseData getAllUnconfirmedList();

    /**
     * 按条件筛选交易记录
     * @param addressList 交易地址列表
     * @return
     */
    public ResponseData getTransactionList(List<String> addressList);

    /**
     * 账号- 交易地址获取对应交易信息
     * @param params
     * @return
     */
    public ResponseData getAccountChainTransaction(TransactionParamsVO params);

    /**
     * 获取地址下余额
     * @param address
     * @param net
     * @return
     */
    public ResponseData getAddressBalance(String address,String net);

    /**
     * 获取公链地址下余额数据 - 地址余额记录入MongoDB库后
     * @param address
     * @param net
     * @return
     */
    public ResponseData getMongoAddressBalance(String address,String net);


    /**
     * 获取用户地址下所有地址对应余额信息以及所有币种统计信息-总价值统计
     * @param addressList
     * @return
     */
    public ResponseData getBalanceStatistics(List<AddressParameterVO> addressList);


    /**
     * 修改存储在内存中的当前块的最大高度  - 六条公链
     * @param map 需要修改的数据
     * @return
     */
    public ResponseData updateBlockHeight(Map<String,Integer> map);

    /**
     * 根据公链以及币种 获取公链地址下余额数据 - 地址余额记录入MongoDB库后
     * @param address
     * @param net
     * @return
     */
    public ResponseData getBalanceBySymbol(String address,String net,String symbol);
}

