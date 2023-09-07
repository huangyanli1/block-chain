package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.AvailablePairsEntity;
import com.block.chain.entity.ExchangeTransactionEntity;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.convert.ExchangeTransactionParamerVO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Exchage Now 跨链桥支持的可用币对
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-10 11:16:06
 */
public interface AvailablePairsService extends IService<AvailablePairsEntity> {


    /**
     *  Exchange Now 可用币对录入接口
     * @return
     */
    public ResponseData saveAvailablePairs();


    /**
     * 币种对应的exchange now可用币对插入redis缓存
     * @return
     */
    public ResponseData getSymbolAvailablePairs();

    /**
     * 通过公链net和symbol获取对应可兑换币对
     * @param net
     * @param symbol
     * @return
     */
    public ResponseData getRedisAvailablePairs(String net ,String symbol);


    /**
     * 拉取exchange- now可用币种并入库
     * @return
     */
    public ResponseData saveAvailableCurrencies();

    /**
     * 获取exchange now 跨链桥可用可兑换币种
     * @return
     */
    public ResponseData getAvailableCurrencies();

    /**
     * 获取exchange now跨链桥兑换币种 发出币可用最低兑换到的得到币数量
     * @param fromCurrency 发送币币种
     * @param fromNetwork  发送币所在公链
     * @param toCurrency   得到币币种
     * @param toNetwork    得到币所在公链
     * @return
     */
    public ResponseData  getMinAmount(String fromCurrency ,String fromNetwork,String toCurrency,String toNetwork);

    /**
     * 获取exchange now跨链桥兑换币种 发出币最低兑换到的得到币数量  - 正反方向
     * @param fromCurrency 发送币币种
     * @param fromNetwork  发送币所在公链
     * @param toCurrency   得到币币种
     * @param toNetwork    得到币所在公链
     * @return
     */
    public ResponseData  getPositiveNegativeAmount(String fromCurrency ,String fromNetwork,String toCurrency,String toNetwork);

    /**
     * 获取exchange now跨链桥兑换币种 发出币预计兑换到的得到币数量
     * @param fromCurrency 发送币币种
     * @param fromNetwork  发送币所在公链
     * @param toCurrency   得到币币种
     * @param toNetwork    得到币所在公链
     * @return
     */
    public ResponseData  getEtimatedAmount(String fromCurrency , String fromNetwork, String toCurrency, String toNetwork, BigDecimal fromAmount);

    /**
     * 跨链桥 - 调用exchange now跨链桥创建兑换交易
     * @param vo 创建兑换交易参数
     * @return
     */
    public ResponseData  createExchangeTransaction(ExchangeTransactionParamerVO vo);

    /**
     * 处理跨链桥订单交易状态
     * 订单创建超过两小时还未处理的视为交易订单失败
     */
    public ResponseData updateTransactionStatus();

//    /**
//     * 兑换交易发送消息的公共方法
//     */
//    public void sendMessage(ExchangeTransactionEntity entity);
}

