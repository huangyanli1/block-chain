package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.CurrencyChainInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公链信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-18 14:12:58
 */
@Mapper
public interface CurrencyChainInfoMapper extends BaseMapper<CurrencyChainInfoEntity> {


    /**
     * 获取所有的公链信息
     * @return
     */
    public List<CurrencyChainInfoEntity> getAllChainInfo();

    /**
     * 获取是否有这条公链
     * @param net
     * @return
     */
    int getNetInfoCount(@Param("net") String net);


}
