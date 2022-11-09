package com.biz.job;

import com.biz.dao.CurrencyConfigDao;
import com.biz.dao.MemberDao;
import com.biz.dao.MemberMoneyDao;
import com.biz.dao.MoneyApplicationDao;
import com.biz.dao.PayRecordDao;
import com.biz.entity.CurrencyConfig;
import com.biz.entity.Member;
import com.biz.entity.MemberMoney;
import com.biz.entity.QMember;
import com.biz.service.MemberMoneyService;
import com.biz.service.MemberService;
import com.biz.service.PayRecordService;
import lombok.extern.slf4j.Slf4j;
import one.yiran.dashboard.entity.SysChannel;
import one.yiran.dashboard.service.SysChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户的一些汇总金额计算，取款
 */
@ConditionalOnProperty(name = "dashboard.job.enable", havingValue = "true")
@Component
@Slf4j
public class MemberMoneyStatsTask {

    @Autowired
    private MemberDao memberDao;
    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberMoneyDao userMoneyDao;
    @Autowired
    private MemberMoneyService userMoneyService;

    @Autowired
    private MoneyApplicationDao moneyApplicationDao;

    @Autowired
    private PayRecordDao payRecordDao;

    @Autowired
    private CurrencyConfigDao currencyConfigDao;

    @Autowired
    private SysChannelService sysChannelService;

    @Autowired
    private PayRecordService payRecordService;

    @Scheduled(cron = "10 */1 * * * ?")
    public void excuteTask() {
        log.info("UserMoneyStatsTask start");
        refresh();
        log.info("UserMoneyStatsTask end");
    }

    private void refresh() {
        List<SysChannel> partnerChannelList = sysChannelService.selectAll();
        partnerChannelList.stream().forEach(partner -> {
            List<Member> userList = memberService.selectList(QMember.member.channelId.eq(partner.getChannelId()));
            List<CurrencyConfig> currencyConfigList = currencyConfigDao.findAllByChannelIdOrderBySortNoAsc(partner.getChannelId());
            for (Member u : userList) {
                Long userId = u.getMemberId();
                currencyConfigList.stream().forEach(t -> {
                    BigDecimal withdrawingAmount = moneyApplicationDao.sumWithdrawingAmount(userId, t.getCurrency());
                    BigDecimal withdrawedAmount = moneyApplicationDao.sumWithdrawedAmount(userId, t.getCurrency());
//                    BigDecimal withdrawedAmount = payRecordDao.sumPayAmount(userId, MoneyTypeEnum.WITHDRAW_MONEY.getType(), t.getCurrency());
                    userMoneyService.updateUserWithdraw(userId, t.getCurrency(), withdrawingAmount, withdrawedAmount);
                });

                try {
                    memberService.tryInitMemberMoney(userId);
                } catch (Exception e) {
                    log.error("创建用户余额账户异常",e);
                }

                try {
                    //测试代码，检查用户余额是否一致
                    currencyConfigList.stream().forEach(t -> {
                        MemberMoney userMoney = userMoneyDao.findByMemberIdAndCurrency(u.getMemberId(), t.getCurrency());
                        BigDecimal dbMoney = userMoney.getAvailableAmount().add(userMoney.getUnavailableAmount());
                        BigDecimal sumTxn = payRecordService.sumTxnAmountByUserIdAndCurrency(u.getMemberId(), t.getCurrency());
                        if (dbMoney.compareTo(sumTxn) != 0) {
                            log.error("用户{}余额异常，通证{}, {} - {} ", u.getMemberId(), t.getCurrency(), dbMoney, sumTxn);
                            userMoney.setAvailableAmount(sumTxn.subtract(userMoney.getUnavailableAmount()));
                            userMoneyDao.saveAndFlush(userMoney);
                        }
                    });
                } catch (Exception e) {
                    log.error("测试余额异常", e);
                }
            }
        });
    }
}
