package com.block.chain.scheduled;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyTypeEntity;
import com.block.chain.mapper.CurrencyTypeMapper;
import com.block.chain.mapper.QuotationMapper;
import com.block.chain.service.MongoDBService;
import com.block.chain.utils.Constant;
import com.block.chain.vo.QuotationVO;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 后台管理系统 - 首页统计 - 接收统计
 */
@Slf4j
@Component
public class ReceiveStatisticSchedule {

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

//    暂不使用
//    @Scheduled(cron = "0 0 */1 * * ?")
    @SchedulerLock(name = "ReceiveStatisticSchedule", lockAtMostFor = Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void visitCountTaskByOneHour(){
        mongoDBService.currencyReceiveStatistics();
    }
//    暂不使用
//    @Scheduled(cron = "0 0 */1 * * ?")
    @SchedulerLock(name = "CompanyReceiveStatisticSchedule", lockAtMostFor = Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void CompanyReceiveStatisticSchedule(){
        mongoDBService.currencyCompanyReceiveStatistics();
    }
}
