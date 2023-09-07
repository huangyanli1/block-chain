package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.StatisticInfoEntity;
import com.block.chain.entity.WalletTransactionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 统计信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-25 18:03:45
 */
@Mapper
public interface StatisticInfoMapper extends BaseMapper<StatisticInfoEntity> {


    /**
     * 根据起止时间查询统计数据
     * @param startTime 开始时间
     * @param endTime  结束时间
     * @return
     */
    List<StatisticInfoEntity> getStatisticInfoList(@Param("startTime") Long startTime, @Param("endTime") Long  endTime);



}
