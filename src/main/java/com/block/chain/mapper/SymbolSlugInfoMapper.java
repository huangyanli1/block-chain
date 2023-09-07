package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.SymbolSlugInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * symbol-slug对应信息表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-12-09 14:16:09
 */
@Mapper
public interface SymbolSlugInfoMapper extends BaseMapper<SymbolSlugInfoEntity> {

    /**
     * 获取symbol - slug对应关系信息
     * @param symbol
     * @return
     */
    int getSymbolSlugCount(@Param("symbol") String symbol);


}
