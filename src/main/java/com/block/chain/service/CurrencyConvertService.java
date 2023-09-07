package com.block.chain.service;


import com.alibaba.fastjson.JSONObject;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.TransactionAddressVO;
import com.block.chain.vo.convert.ExchangeRateParamerVO;

import java.util.List;
import java.util.Map;

/**
 * 币币兑换相关业务功能实现类 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-12-07
 */
public interface CurrencyConvertService {

    /**
     * 币币兑换 - 币种转化汇率查询获取
     * 入账币种兑换出账币种数量
     * @return
     */
    public ResponseData getEstimatedExchangeRate(ExchangeRateParamerVO vo);

    /**
     * 币币兑换 - 用户点击交易按钮时 - 获取点击时刻入账出账币种汇率并和上一次汇率比较偏差是否大于一个滑点（即百分之一）
     * 入账币种兑换出账币种数量
     * @return
     */
    public ResponseData getPreciseExchangeRate(ExchangeRateParamerVO vo);

    /**
     * 转发前端接口，调用链上业务信息接口
     * @param json
     * @return
     */
    public Object transferOnChain(JSONObject json);

}

