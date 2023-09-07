package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.StatisticAddressEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公司地址下数据统计表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-29 13:14:32
 */
@Mapper
public interface StatisticAddressMapper extends BaseMapper<StatisticAddressEntity> {

    /**
     * 清空所有地址统计记录
     * @return
     */
    public int remove();


    /**
     * 获取地址下
     * @param address
     * @return
     */
    List<StatisticAddressEntity> getStatisticAddressList(@Param("net") String net,@Param("address") String address);
}
