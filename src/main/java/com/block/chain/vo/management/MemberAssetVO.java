package com.block.chain.vo.management;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 后台管理系统 - 会员管理 - 会员基础信息 - 加密数字资产管理 - 统计信息VO
 */
@Data
public class MemberAssetVO {

    /**
     * 类型(ETH BTC TRX MATIC HECO BSC)
     */
    private String net;

    /**
     * 交易币种符号
     */
    private String symbol;

    /**
     * 币种余额
     */
    private String balance;

//    /**
//     * 历史接收数量
//     */
//    private Integer receiveCount;
//
//    /**
//     * 历史发送数量
//     */
//    private Integer sendingCount;

    /**
     * 历史接收币数量
     */
    private String receiveCount;

    /**
     * 历史发送币数量
     */
    private String sendingCount;

    /**
     * 历史兑换币数量
     */
    private String exchangeCount;

    /**
     * 钱包地址
     */
    private List<String> address;



}
