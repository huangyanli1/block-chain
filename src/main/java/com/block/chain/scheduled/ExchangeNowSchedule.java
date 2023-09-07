package com.block.chain.scheduled;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.block.chain.entity.CurrencyTypeEntity;
import com.block.chain.mapper.CurrencyTypeMapper;
import com.block.chain.mapper.QuotationMapper;
import com.block.chain.service.AvailablePairsService;
import com.block.chain.service.ExchangeTransactionService;
import com.block.chain.service.MessagePushFailService;
import com.block.chain.service.impl.AvailablePairsServiceImpl;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 兑换相关定时任务 - 1hour线
 */
@Slf4j
@Component
public class ExchangeNowSchedule {
    @Autowired
    private AvailablePairsService availablePairsService;

    @Autowired
    private MessagePushFailService messagePushFailService;


    @Scheduled(cron = "00 */1 * * * ?")
    @SchedulerLock(name = "ExchangeNowSchedule", lockAtMostFor = Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void visitCountTaskByOneHour(){
        availablePairsService.updateTransactionStatus();
    }

}
