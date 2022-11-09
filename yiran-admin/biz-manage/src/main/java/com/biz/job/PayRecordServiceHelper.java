package com.biz.job;

import com.biz.constants.AccountTypeEnum;
import com.biz.dao.PayRecordDao;
import com.biz.entity.PayRecord;
import com.biz.service.PayRecordService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.common.util.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "dashboard.job.enable", havingValue = "true")
@Slf4j
public class PayRecordServiceHelper {
    @Autowired
    private PayRecordService payRecordService;

    @Autowired
    private PayRecordDao payRecordDao;

    @Getter
    ScheduledExecutorService userMoneyUpdater = null;

    @PostConstruct
    public void setUp() {
        userMoneyUpdater = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setNameFormat("user-money-updater").build());
        userMoneyUpdater.scheduleAtFixedRate(() -> {
            try {
                updateUserMoneyJob();
            } catch (Throwable e) {
                log.info("异步加钱job异常", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    private void updateUserMoneyJob() {
        LocalDateTime now = LocalDateTime.now().minusSeconds(30);
        LocalDateTime start = now.minusDays(7); // default last 7 days, unless it run success once
        LocalDateTime end = now;
        try {
            processMoneyReturnRecords(start, end);
        } catch (Throwable throwable) {
            log.error("order update expired status error", throwable);
        } finally {
        }
    }

    private void processMoneyReturnRecords(LocalDateTime start, LocalDateTime end) {
        Set<Long> payRecords = payRecordDao.findMoneyReturnToProcess(start, end);
        for (Long id : payRecords) {
            try {
                //处理异步加钱payRecords
                log.info("定时，处理异步加钱payRecords {}", id);
                PayRecord payRecord = payRecordDao.findById(id).orElse(null);
                if (payRecord != null) {
                    log.info("定时，处理异步加钱payRecords 用户ID {}", payRecord.getMemberId());
                    payRecordService.updateUserMoney(payRecord.getMemberId(), payRecord.getId());
                }
            } catch (Throwable throwable) {
                log.error("order update expired status process money return records error", throwable);
            }
        }
    }

    public static void triggerAsync(Long payRecordId) {
        PayRecordServiceHelper payRecordServiceHelper = SpringUtil.getBean(PayRecordServiceHelper.class);
        if(payRecordServiceHelper == null)
            return;
        payRecordServiceHelper.getUserMoneyUpdater().schedule(() -> {
            PayRecordDao payRecordDao = SpringUtil.getBean(PayRecordDao.class);
            PayRecordService payRecordService = SpringUtil.getBean(PayRecordService.class);
            PayRecord payRecord = payRecordDao.findById(payRecordId).orElse(null);
            if (payRecord != null && payRecord.getAccountType().intValue() == AccountTypeEnum.MONEY.getCode()) {
                log.info("手动，处理异步加钱payRecords 会员ID {}", payRecord.getMemberId());
                payRecordService.updateUserMoney(payRecord.getMemberId(), payRecord.getId());
            } else {
                log.error("payRecord异常");
            }
        }, 100, TimeUnit.MILLISECONDS);
    }
}
