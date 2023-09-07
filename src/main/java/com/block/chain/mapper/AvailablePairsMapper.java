package com.block.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.block.chain.entity.AvailablePairsEntity;
import com.block.chain.entity.WalletCurrencyTransactionEntity;
import com.block.chain.vo.QuotationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Exchage Now 跨链桥支持的可用币对
 * 
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-10 11:16:06
 */
@Mapper
public interface AvailablePairsMapper extends BaseMapper<AvailablePairsEntity> {

    /**
     * 清空所有从exchange-now获取的可用币种
     * @return
     */
    public int remove();


    /**
     * 批量插入exchange now可用币对
     * @param data
     * @return
     */
    int  insertAvailablePairs(@Param("data") AvailablePairsEntity data);

    /**
     *
     * @param symbol 币种
     * @param net 公链
     * @return
     */
    public List<AvailablePairsEntity> getAvailablePairsList(@Param("net")  String net, @Param("symbol") String symbol);

    /**
     *
     * @param symbol 币种
     * @param net 公链
     * @return
     */
    public List<AvailablePairsEntity> getAvailablePairsOppositeDirectionList(@Param("net")  String net, @Param("symbol") String symbol);



}
