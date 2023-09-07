package com.block.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.block.chain.entity.QuotationHourEntity;
import com.block.chain.utils.ResponseData;
import com.block.chain.vo.QuotationParamerVO;
import org.springframework.scheduling.annotation.Async;


import java.util.List;
import java.util.Map;

/**
 * 所有币种的小时K线
 *
 * @author michael
 * @email 123456789@qq.com
 * @date 2023-02-16 18:00:41
 */
public interface QuotationHourService extends IService<QuotationHourEntity> {


    /**
     * 所有币种小时K线行情录入
     */
    public void saveQuotationHour();

    /**
     * 补全交易记录中的coinPrice,coinValue字段
     * coinPrice
     * coinValue
     */
    public void addCompletionTransaction();

    /**
     * 所有币种昨日小时K线行情录入
     */
    public void saveYesterdayQuotationHour();

    public ResponseData getQuotationList(List<QuotationParamerVO> list);
}

