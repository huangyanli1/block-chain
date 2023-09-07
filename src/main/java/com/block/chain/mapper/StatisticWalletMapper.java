package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.StatisticWalletEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公司钱包数据统计表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-29 13:14:32
 */
@Mapper
public interface StatisticWalletMapper extends BaseMapper<StatisticWalletEntity> {

    /**
     * 清空所有公链统计记录
     * @return
     */
    public int remove();

    /**
     * 获取所有公链下统计信息
     * @return
     */
    List<StatisticWalletEntity> getWalletInfoList();
}
