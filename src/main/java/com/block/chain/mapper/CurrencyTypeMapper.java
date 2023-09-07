package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.block.chain.entity.CurrencyTypeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 币种配置
 * 
 * @author michael
 * @email 123456@qq.com
 * @date 2022-10-10 09:33:44
 */
@Mapper
public interface CurrencyTypeMapper extends BaseMapper<CurrencyTypeEntity> {

    /**
     * 获取所有币种信息
     * @return
     */
    List<CurrencyTypeEntity> getCurrencyList();

    /**
     * 按条件筛选币种信息
     * @return
     */
    List<CurrencyTypeEntity> getCurrencyListByParamer(@Param("id") Long id,@Param("isBuy") Integer isBuy,@Param("isExchange") Integer isExchange,@Param("net") String net,@Param("symbol") String symbol,@Param("currencyStatus") Integer currencyStatus,@Param("coinType") Integer coinType,@Param("page") Integer page, @Param("pageSize") Integer pageSize);
    Integer getCurrencyListByParamerCount(@Param("id") Long id,@Param("isBuy") Integer isBuy,@Param("isExchange") Integer isExchange,@Param("net") String net,@Param("symbol") String symbol,@Param("currencyStatus") Integer currencyStatus,@Param("coinType") Integer coinType,@Param("page") Integer page, @Param("pageSize") Integer pageSize);



    /**
     * 获取公链为ETH和MATIC下可兑换币种
     * @return
     */
    List<CurrencyTypeEntity> getExchangeList();

    /**
     * 获取所有可兑换币种
     * @return
     */
    List<CurrencyTypeEntity> getAllExchangeList();


    /**
     * 获取exchange now 跨链桥可用可兑换币种
     * @return
     */
    List<CurrencyTypeEntity> getAvailableCurrencies();

    /**
     * 按公链和币种获取币种信息
     * @param net
     * @param symbol
     * @return
     */
    CurrencyTypeEntity getCurrencyInfo(@Param("net") String net,@Param("symbol") String symbol);

    /**
     * 按公链和 币种类型获取主链信息
     * @param net
     * @return
     */
    CurrencyTypeEntity getMainCurrencyInfo(@Param("net") String net);

    /**
     * 取系统所有支持的链
     * @return
     */
    List<String>  getChainList();


    /**
     * 按合约地址获取对应的币种信息
     * @param contractAddress
     * @return
     */
    CurrencyTypeEntity getCurrencyContract(@Param("contractAddress") String contractAddress);


}
