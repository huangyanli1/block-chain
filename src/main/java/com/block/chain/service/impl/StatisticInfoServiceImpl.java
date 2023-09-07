package com.block.chain.service.impl;

import com.block.chain.entity.StatisticInfoEntity;
import com.block.chain.mapper.StatisticInfoMapper;
import com.block.chain.service.StatisticInfoService;
import com.block.chain.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;



@Service("statisticInfoService")
public class StatisticInfoServiceImpl extends ServiceImpl<StatisticInfoMapper, StatisticInfoEntity> implements StatisticInfoService {

    @Autowired
    private StatisticInfoMapper statisticInfoMapper;

    /**
     * 根据起止时间获取统计数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    public ResponseData getStatisticInfoList(Long startTime ,Long endTime){
        List<StatisticInfoEntity> list = new ArrayList<>();
        list = statisticInfoMapper.getStatisticInfoList(startTime,endTime);
        return ResponseData.ok(list);
    }

}