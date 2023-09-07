package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.AvailablePairsEntity;
import com.block.chain.entity.QuotationHourEntity;
import com.block.chain.vo.QuotationParamerVO;
import com.block.chain.vo.QuotationResultVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 所有币种的小时K线
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-16 18:00:41
 */
@Mapper
public interface QuotationHourMapper extends BaseMapper<QuotationHourEntity> {
    /**
     * 批量插入exchange now可用币对
     * @param data
     * @return
     */
    int  insertQuotationHour(@Param("data") QuotationHourEntity data);

    List<QuotationHourEntity> getHourPrice(@Param("startTime") Long startTime, @Param("endTime") Long  endTime,@Param("symbol") String  symbol);

    List<QuotationResultVO> getNearestData(@Param("paramerList") List<QuotationParamerVO> paramerList);
}
