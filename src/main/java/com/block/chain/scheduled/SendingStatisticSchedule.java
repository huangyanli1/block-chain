package com.block.chain.scheduled;

import com.block.chain.mapper.CurrencyTypeMapper;
import com.block.chain.mapper.QuotationMapper;
import com.block.chain.service.CurrencyRateService;
import com.block.chain.service.MongoDBService;
import com.block.chain.service.QuotationHourService;
import com.block.chain.utils.Constant;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 后台管理系统 - 首页统计 - 发送统计
 */
@Slf4j
@Component
public class SendingStatisticSchedule {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CurrencyTypeMapper currencyTypeMapper;

    @Autowired
    public QuotationMapper quotationMapper;

    @Value("${BLOCKCC.KlineUrl}")
    private String klineUrl;

    @Value("${BLOCKCC.ApiKey}")
    private String apiKey;

    @Autowired
    private MongoDBService mongoDBService;

    @Autowired
    private QuotationHourService quotationHourService;





//    暂不使用
//    @Scheduled(cron = "0 0 */1 * * ?")
    @SchedulerLock(name = "SendingStatisticSchedule", lockAtMostFor = Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void visitCountTaskByOneHour(){
        mongoDBService.currencySendingStatistics();
    }


    /**
     * 所有币种昨日小时K线行情录入定时任务
     */
    @Scheduled(cron = "0 33 */1 * * ?")
    @SchedulerLock(name = "SaveQuotationHourSchedule", lockAtMostFor = Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void SaveQuotationHourSchedule(){
        quotationHourService.saveYesterdayQuotationHour();
    }


}
