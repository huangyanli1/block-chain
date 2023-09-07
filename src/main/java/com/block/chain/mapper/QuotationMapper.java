package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.QuotationEntity;
import com.block.chain.vo.IntervalVO;
import com.block.chain.vo.QuotationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
@Mapper
public interface QuotationMapper extends BaseMapper<QuotationEntity> {


    /**
     * 按条件筛选历史行情数据
     * vo 查询参数
     * @return
     */
    List<QuotationEntity>  getQuotationList(IntervalVO vo);

    /**
     * 按条件筛选历史行情数据 - 当开始或结束时间为空- 取固定数量
     * vo 查询参数
     * @return
     */
    List<QuotationEntity>  getQuotationSizeList(IntervalVO vo);


    /**
     * 根据传入表名，自动创建表
     * @param
     * @return
     */
    int automaticCreateTable(@Param("tableName") String tableName);


    int isHaveTable(@Param("tableName") String tableName);

    int  insertQuotation(QuotationVO vo);

}
