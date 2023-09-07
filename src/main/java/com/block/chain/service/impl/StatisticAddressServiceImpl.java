package com.block.chain.service.impl;

import com.block.chain.entity.StatisticAddressEntity;
import com.block.chain.mapper.StatisticAddressMapper;
import com.block.chain.service.StatisticAddressService;
import com.block.chain.utils.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;



@Service("statisticAddressService")
public class StatisticAddressServiceImpl extends ServiceImpl<StatisticAddressMapper, StatisticAddressEntity> implements StatisticAddressService {


    @Autowired
    private StatisticAddressMapper statisticAddressMapper;


    /**
     * 获取钱包地址下统计信息
     * @param address
     * @return
     */
    public ResponseData getStatisticAddressList(String net ,String address){
        List<StatisticAddressEntity> list = statisticAddressMapper.getStatisticAddressList(net,address);
        return ResponseData.ok(list);
    }

}