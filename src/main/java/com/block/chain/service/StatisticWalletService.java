package com.block.chain.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.StatisticWalletEntity;
import com.block.chain.utils.ResponseData;



/**
 * 公司钱包数据统计表
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-29 13:14:32
 */
public interface StatisticWalletService extends IService<StatisticWalletEntity> {

    /**
     * 获取所有公链下统计信息-包含总价值统计
     * @return
     */
    public ResponseData getWalletInfoList();

    /**
     * 公司钱包数据统计录入
     * @param json 钱包数据统计信息
     * @return
     */
    public ResponseData saveWalletInfo(JSONObject json);


    /**
     * 所有公司地址下今日划转，提现数量统计以及历史划转，提现数量统计
     * @return
     */
    public ResponseData getHistoryStatisticInfo();
}

