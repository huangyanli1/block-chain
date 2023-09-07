package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.StatisticAddressEntity;
import com.block.chain.utils.ResponseData;



/**
 * 公司地址下数据统计表
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-29 13:14:32
 */
public interface StatisticAddressService extends IService<StatisticAddressEntity> {
    /**
     * 获取钱包地址下统计信息
     * @param address
     * @return
     */
    public ResponseData getStatisticAddressList(String net ,String address);


}

