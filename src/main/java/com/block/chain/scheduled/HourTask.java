package com.block.chain.scheduled;

import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class HourTask {
    /**
     * 最小锁定时间,一般设置成定时任务小一点
     */
    private static final int MIN_LOCK_TIME = 1000;//单位毫秒
    /**
     * 最大锁定时间,一般设置成比正常执行时间长的值
     */
    private static final int MAX_LOCK_TIME = 1000 * 2;//单位毫秒


//    @Scheduled(cron = "0 */1 * * * ?")
    @SchedulerLock(name = "测试", lockAtMostFor = MAX_LOCK_TIME, lockAtLeastFor = MIN_LOCK_TIME)
    public void visitCountTaskByTwoHour() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("现在时间是" + format.format(new Date())+ Thread.currentThread().getName());
    }

}
