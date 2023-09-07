package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.QuotationEntity;
import com.block.chain.utils.R;
import com.block.chain.utils.ResponseData;


/**
 * 
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2022-10-12 10:17:17
 */
public interface QuotationService extends IService<QuotationEntity> {

    /**
     * 按条件筛选历史行情数据
     * @param symbol 币种符号
     * @param intervalType K线间隔
     * @param startTime 开始时间(时间戳)
     * @param endTime 结束时间(时间戳)
     * @return
     */
    public ResponseData getQuotationList(String symbol, String intervalType, String startTime , String endTime);

    /**
     * 自动建表
     * @param tableName 表名
     * @return
     */
    public R automaticCreateTable(String tableName);

    /**
     * 获取币种价格
     * @param url 查询URL
     * @return
     */
    public R getCurrencyPrice(String url);

    /**
     * 判断数据库中是否存在某一张表
     * @param tableName
     * @return
     */
    public R getIsHaveTable(String tableName);
}

