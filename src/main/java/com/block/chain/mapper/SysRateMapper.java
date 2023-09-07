package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.SysRateEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 系统汇率表
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-06-05 17:16:12
 */
@Mapper
public interface SysRateMapper extends BaseMapper<SysRateEntity> {

    /**
     * 获取所有的法币symbol
     * @return
     */
    List<String> getAllSymbol();
	
}
