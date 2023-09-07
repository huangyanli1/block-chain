package com.block.chain.scheduled;

import com.block.chain.service.CurrencyRateService;
import com.block.chain.service.MessagePushFailService;
import com.block.chain.utils.Constant;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 消息和汇率相关得定时任务
 */
@Slf4j
@Component
public class CurrencyRateSchedule {
    @Autowired
    private MessagePushFailService messagePushFailService;

    @Autowired
    private CurrencyRateService currencyRateService;

    /**
     * 每五分钟调用一遍推送失败消息存储表，用来处理推送失败的消息
     */
//  @Scheduled(cron = "00 */5 * * * ?")
    @SchedulerLock(name = "handleFailMessageSchedule", lockAtMostFor = Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void handleFailMessage(){
        messagePushFailService.handleFailMessage();
    }


    /**
     * 汇率录入接口 - 主要录入接口
     */
    @Scheduled(cron = "0 42 */4 * * ?")
    @SchedulerLock(name = "CurrencyRateSchedule", lockAtMostFor = Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void integratedRateByThreeHour(){
        currencyRateService.integratedRate();
    }

    /**
     * 获取所有法币，并集成法币汇率 - 汇率集成的录入补充接口
     */
    @Scheduled(cron = "0 34 04 */1 * ?")
    @SchedulerLock(name = "IntegratedSupplementaryRateSchedule", lockAtMostFor = Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void integratedSupplementaryRate(){
        currencyRateService.integratedSupplementaryRate();
    }

    /**
     * 更新所有法币对应的美元汇率 - 通过蜜蜂查进行更新集成
     */
    @Scheduled(cron = "0 13 */4 * * ?")
    @SchedulerLock(name = "mifengchaExchangeRateSchedule", lockAtMostFor = Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void mifengchaExchangeRate(){
        currencyRateService.mifengchaExchangeRate();
    }

    /**
     * 推送所有更新的法币汇率 - 仅仅适用于消息推送
     */
    @Scheduled(cron = "0 08 */1 * * ?")
    @SchedulerLock(name = "sendFiatRateMessageSchedule", lockAtMostFor = Constant.MAX_LOCK_TIME, lockAtLeastFor = Constant.MIN_LOCK_TIME)
    public void sendFiatRateMessageOneHour(){
        currencyRateService.sendFiatRateMessage();
    }

}
