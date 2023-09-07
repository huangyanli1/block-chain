package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.StatisticInfoEntity;
import com.block.chain.utils.ResponseData;



/**
 * 统计信息表
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-25 18:03:45
 */
public interface StatisticInfoService extends IService<StatisticInfoEntity> {

    /**
     * 根据起止时间获取统计数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    public ResponseData getStatisticInfoList(Long startTime , Long endTime);

}

